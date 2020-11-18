package ru.gorinych3.inetshop.dao;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.gorinych3.inetshop.connectionmanager.ConnectionManagerJdbcImpl;
import ru.gorinych3.inetshop.dto.Item;
import ru.gorinych3.inetshop.dto.Order;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@SuppressWarnings("SqlResolve")
public class JdbcOrderDaoImpl implements JdbcOrderDao {

    private static final Logger LOGGER = LogManager.getLogger(JdbcOrderDaoImpl.class.getName());

    private final JdbcItemDao jdbcItemDao;

    public JdbcOrderDaoImpl(JdbcItemDao jdbcItemDao) {
        this.jdbcItemDao = jdbcItemDao;
    }

    @Override
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();

        try (CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
             Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection()) {
            cachedRowSet.setCommand("SELECT * FROM orders");
            cachedRowSet.execute(connection);

            return initOrderList(orders, cachedRowSet);
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
        return orders;
    }

    @Override
    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = new ArrayList<>();

        try (CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
             Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM orders WHERE status = (?)")) {

            preparedStatement.setString(1, status);
            cachedRowSet.populate(preparedStatement.executeQuery());

            return initOrderList(orders, cachedRowSet);
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
        return orders;
    }


    @Override
    public Order getOrderById(BigDecimal orderId) {
        Order order = new Order();
        try (CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
             Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM orders WHERE orderId = (?)")) {

            return initOrder(orderId, order, cachedRowSet, preparedStatement);
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }

        return order;
    }

    @Override
    public List<Order> getOrderByClientId(BigDecimal clientId) {
        List<Order> orders = new ArrayList<>();

        try (CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
             Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM orders WHERE clientId = (?)")) {

            preparedStatement.setBigDecimal(1, clientId);
            cachedRowSet.populate(preparedStatement.executeQuery());

            return initOrderList(orders, cachedRowSet);
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
        return orders;
    }


    /**
     * В данном методе применил батчинг (несколько SQL запросов в пачке), а так же использовал
     * ручное управление транзакциями (Savepoint, rollback)
     *
     * @param order
     * @return
     */
    @Override
    public Order addOrder(Order order) {
        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement psOrders = connection.prepareStatement(
                     "INSERT INTO orders VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psOrderItems = connection.prepareStatement(
                     "INSERT INTO orderItems VALUES(DEFAULT, ?, ?)")) {

            connection.setAutoCommit(false);

            Savepoint insertOrder = connection.setSavepoint("insertOrder");

            psOrders.setInt(1, order.getItems().size());
            psOrders.setBigDecimal(2, sum(order.getItems()));
            psOrders.setString(3, order.getStatus());
            psOrders.setTimestamp(4, Timestamp.valueOf(order.getOpenDate()));
            psOrders.setTimestamp(5, null);
            psOrders.setBigDecimal(6, order.getClientId());

            psOrders.executeUpdate();
            try (ResultSet generatedKeys = psOrders.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setOrderId(generatedKeys.getBigDecimal(1));
                }
            }

            try {
                for (Item item : order.getItems()) {
                    psOrderItems.setBigDecimal(1, order.getOrderId());
                    psOrderItems.setBigDecimal(2, item.getItemId());
                    psOrderItems.addBatch();
                }

                psOrderItems.executeBatch();

                for (Item item : order.getItems()) {
                    item.setStatus("O");
                    if (Objects.equals(jdbcItemDao.updateItem(item), null)) {
                        throw new SQLException("Проблема при обновлении статуса товара. Операция отменена");
                    }
                }

                connection.commit();

                return getOrderById(order.getOrderId());
            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
                connection.rollback(insertOrder);
                connection.setAutoCommit(true);
            }

            connection.setAutoCommit(true);

        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
        }
        return new Order();
    }

    @Override
    public boolean addItem2Order(Item item, BigDecimal orderId) {
        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement psOrderItems = connection.prepareStatement(
                     "INSERT INTO orderItems VALUES(DEFAULT, ?, ?)")) {

            connection.setAutoCommit(false);

            psOrderItems.setBigDecimal(1, orderId);
            psOrderItems.setBigDecimal(2, item.getItemId());

            psOrderItems.execute();

            item.setStatus("O");
            if (Objects.equals(jdbcItemDao.updateItem(item), null)) {
                connection.rollback();
                connection.setAutoCommit(true);
                throw new SQLException("Проблема при обновлении статуса товара. Операция отменена");
            }

            connection.commit();
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteItemFromOrder(BigDecimal orderId, BigDecimal itemId) {
        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "DELETE FROM orderItems where orderId = (?) and itemId = (?)")) {

            preparedStatement.setBigDecimal(1, orderId);
            preparedStatement.setBigDecimal(2, itemId);
            preparedStatement.executeUpdate();

            Item changedItem = jdbcItemDao.getItemById(itemId);
            changedItem.setStatus("N");
            jdbcItemDao.updateItem(changedItem);

            return true;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }

    @Override
    public Order changeOrder(Order order) {
        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE orders SET countItems = (?), sum = (?), status = (?)," +
                             "openDate = (?), executeDate = (?), clientId = (?) where orderId = (?)")) {
            preparedStatement.setInt(1, order.getItems().size());
            preparedStatement.setBigDecimal(2, sum(order.getItems()));
            preparedStatement.setString(3, order.getStatus());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(order.getOpenDate()));
            preparedStatement.setTimestamp(5, order.getExecuteDate() == null ?
                    null : Timestamp.valueOf(order.getExecuteDate()));
            preparedStatement.setBigDecimal(6, order.getClientId());
            preparedStatement.setBigDecimal(7, order.getOrderId());
            preparedStatement.executeUpdate();

            return getOrderById(order.getOrderId());
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return order;
    }

    @Override
    public boolean deleteOrder(BigDecimal orderId) {
        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement psOrders = connection.prepareStatement(
                     "DELETE FROM orders where orderId = (?)");
             PreparedStatement psOrderItems = connection.prepareStatement(
                     "DELETE FROM orderItems where orderId = (?)")) {

            connection.setAutoCommit(false);
            Savepoint init = connection.setSavepoint("init");

            try {
                psOrders.setBigDecimal(1, orderId);
                psOrders.executeUpdate();

                psOrderItems.setBigDecimal(1, orderId);
                psOrderItems.executeUpdate();
            } catch (SQLException exception) {
                connection.rollback(init);
                connection.setAutoCommit(true);
                throw new SQLException(exception);
            }

            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }


    private List<Order> initOrderList(List<Order> orders, CachedRowSet cachedRowSet) throws SQLException {
        Order order = new Order();
        while (cachedRowSet.next()) {
            order.setOrderId(cachedRowSet.getBigDecimal("orderId"));
            order.setCountItems(cachedRowSet.getInt("countItems"));
            order.setSum(cachedRowSet.getBigDecimal("sum"));
            order.setStatus(cachedRowSet.getString("status"));
            order.setOpenDate(cachedRowSet.getTimestamp("opendate").toLocalDateTime());
            LocalDateTime executeDate = cachedRowSet.getTimestamp("executeDate") == null ?
                    null : cachedRowSet.getTimestamp("executeDate").toLocalDateTime();
            order.setExecuteDate(executeDate);
            BigDecimal clientId = cachedRowSet.getBigDecimal("clientId") == null ?
                    new BigDecimal("0") : cachedRowSet.getBigDecimal("clientId");
            order.setClientId(clientId);
            order.setItems(getItemsFromOrder(order.getOrderId()));

            orders.add(order);
        }
        return orders;
    }

    private List<Item> getItemsFromOrder(BigDecimal orderId) {

        List<Item> items = new ArrayList<>();
        Item item;

        try (CachedRowSet cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
             Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT * FROM orderItems WHERE orderId = (?)")) {

            preparedStatement.setBigDecimal(1, orderId);
            cachedRowSet.populate(preparedStatement.executeQuery());

            while (cachedRowSet.next()) {
                item = jdbcItemDao.getItemById(cachedRowSet.getBigDecimal("itemId"));
                items.add(item);
            }
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }

        return items;
    }

    private Order initOrder(BigDecimal clientId, Order order, CachedRowSet cachedRowSet,
                            PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setBigDecimal(1, clientId);
        cachedRowSet.populate(preparedStatement.executeQuery());

        if (cachedRowSet.next()) {
            order.setOrderId(cachedRowSet.getBigDecimal("orderId"));
            order.setCountItems(cachedRowSet.getInt("countItems"));
            order.setSum(cachedRowSet.getBigDecimal("sum"));
            order.setStatus(cachedRowSet.getString("status"));
            order.setOpenDate(cachedRowSet.getTimestamp("openDate").toLocalDateTime());
            LocalDateTime executeDate = cachedRowSet.getTimestamp("executeDate") == null ?
                    null : cachedRowSet.getTimestamp("executeDate").toLocalDateTime();
            order.setExecuteDate(executeDate);
            order.setClientId(cachedRowSet.getBigDecimal("clientId"));
            order.setItems(getItemsFromOrder(order.getOrderId()));
        }
        return order;
    }

    private BigDecimal sum(List<Item> items) {
        BigDecimal sum = new BigDecimal("0");
        for (Item item : items) {
            sum = sum.add(item.getCost());
        }
        return sum;
    }
}
