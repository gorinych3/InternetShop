package ru.gorinych3.inetshop.dao;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gorinych3.inetshop.connectionmanager.ConnectionManager;
import ru.gorinych3.inetshop.dto.Item;
import ru.gorinych3.inetshop.dto.Order;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@SuppressWarnings("SqlResolve")
public class JdbcOrderDaoImpl implements JdbcOrderDao {

    public static final String SELECT_ALL_FROM_ORDERS = "SELECT * FROM orders";
    public static final String SELECT_ALL_FROM_ORDERS_BY_STATUS = "SELECT * FROM orders WHERE status = (?)";
    public static final String SELECT_FROM_ORDERS_BY_ID = "SELECT * FROM orders WHERE orderId = (?)";
    public static final String SELECT_ALL_FROM_ORDERS_BY_CLIENT_ID = "SELECT * FROM orders WHERE clientId = (?)";
    public static final String INSERT_INTO_ORDERS = "INSERT INTO orders VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)";
    public static final String INSERT_INTO_ORDERITEMS = "INSERT INTO orderItems VALUES(DEFAULT, ?, ?)";
    public static final String DELETE_FROM_ORDERITEMS = "DELETE FROM orderItems where orderId = (?) and itemId = (?)";
    public static final String UPDATE_ORDERS = "UPDATE orders SET countItems = (?), sum = (?), status = (?)," +
            "openDate = (?), executeDate = (?), clientId = (?) where orderId = (?)";
    public static final String DELETE_FROM_ORDER_BY_ID = "DELETE FROM orders where orderId = (?)";
    public static final String DELETE_FROM_ORDERITEMS_BY_ORDER_ID = "DELETE FROM orderItems where orderId = (?)";
    public static final String SELECT_ALL_FROM_ORDERITEMS_BY_ORDER_ID = "SELECT * FROM orderItems WHERE orderId = (?)";

    private static final Logger LOGGER = LogManager.getLogger(JdbcOrderDaoImpl.class);
    private final ConnectionManager connectionManager;

    private final JdbcItemDao jdbcItemDao;

    public JdbcOrderDaoImpl(JdbcItemDao jdbcItemDao, ConnectionManager connectionManager) {
        this.jdbcItemDao = jdbcItemDao;
        this.connectionManager = connectionManager;
    }

    @Override
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_FROM_ORDERS)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return initOrderList(orders, resultSet);
            }
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
        return orders;
    }

    @Override
    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = new ArrayList<>();

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_FROM_ORDERS_BY_STATUS)) {

            preparedStatement.setString(1, status);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return initOrderList(orders, resultSet);
            }
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
        return orders;
    }


    @Override
    public Order getOrderById(BigDecimal orderId) {
        Order order = new Order();
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FROM_ORDERS_BY_ID)) {
            preparedStatement.setBigDecimal(1, orderId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return initOrder(resultSet);
            }
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }

        return new Order();
    }

    @Override
    public List<Order> getOrderByClientId(BigDecimal clientId) {
        List<Order> orders = new ArrayList<>();

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_FROM_ORDERS_BY_CLIENT_ID)) {
            preparedStatement.setBigDecimal(1, clientId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return initOrderList(orders, resultSet);
            }
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
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement psOrders = connection.prepareStatement(
                     INSERT_INTO_ORDERS, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psOrderItems = connection.prepareStatement(INSERT_INTO_ORDERITEMS)) {

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
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement psOrderItems = connection.prepareStatement(
                     INSERT_INTO_ORDERITEMS)) {

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
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FROM_ORDERITEMS)) {

            preparedStatement.setBigDecimal(1, orderId);
            preparedStatement.setBigDecimal(2, itemId);
            preparedStatement.executeUpdate();

            Item changedItem = jdbcItemDao.getItemById(itemId);
            changedItem.setStatus("N");
            if (jdbcItemDao.updateItem(changedItem).getItemId() != null) {
                return true;
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }

    @Override
    public Order changeOrder(Order order) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_ORDERS)) {

            preparedStatement.setInt(1, order.getItems().size());
            preparedStatement.setBigDecimal(2, sum(order.getItems()));
            preparedStatement.setString(3, order.getStatus());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(order.getOpenDate()));
            preparedStatement.setTimestamp(5, order.getExecuteDate() == null ?
                    null : Timestamp.valueOf(order.getExecuteDate()));
            preparedStatement.setBigDecimal(6, order.getClientId());
            preparedStatement.setBigDecimal(7, order.getOrderId());
            if (preparedStatement.executeUpdate() != 1) {
                throw new SQLException("Проблема при обновлении ордера. Данные не обновились");
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return new Order();
        }
        return order;
    }

    @Override
    public boolean deleteOrder(BigDecimal orderId) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement psOrders = connection.prepareStatement(DELETE_FROM_ORDER_BY_ID);
             PreparedStatement psOrderItems = connection.prepareStatement(DELETE_FROM_ORDERITEMS_BY_ORDER_ID)) {

            connection.setAutoCommit(false);
            Savepoint init = connection.setSavepoint("init");

            try {
                psOrders.setBigDecimal(1, orderId);
                if (psOrders.executeUpdate() != 1) {
                    throw new SQLException("Проблема при удалении ордера. Операция отменена");
                }

                psOrderItems.setBigDecimal(1, orderId);
                if (psOrderItems.executeUpdate() == 0) {
                    throw new SQLException("Проблема при удалении ордера. Операция отменена");
                }
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


    private List<Order> initOrderList(List<Order> orders, ResultSet resultSet) throws SQLException {
        Order order = new Order();
        while (resultSet.next()) {
            order.setOrderId(resultSet.getBigDecimal("orderId"));
            order.setCountItems(resultSet.getInt("countItems"));
            order.setSum(resultSet.getBigDecimal("sum"));
            order.setStatus(resultSet.getString("status"));
            order.setOpenDate(resultSet.getTimestamp("openDate").toLocalDateTime());
            LocalDateTime executeDate = resultSet.getTimestamp("executeDate") == null ?
                    null : resultSet.getTimestamp("executeDate").toLocalDateTime();
            order.setExecuteDate(executeDate);
            BigDecimal clientId = resultSet.getBigDecimal("clientId") == null ?
                    new BigDecimal("0") : resultSet.getBigDecimal("clientId");
            order.setClientId(clientId);
            order.setItems(getItemsFromOrder(order.getOrderId()));

            orders.add(order);
        }
        return orders;
    }

    private List<Item> getItemsFromOrder(BigDecimal orderId) {

        List<Item> items = new ArrayList<>();
        if (orderId == null) return items;
        Item item;

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     SELECT_ALL_FROM_ORDERITEMS_BY_ORDER_ID)) {

            preparedStatement.setBigDecimal(1, orderId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    item = jdbcItemDao.getItemById(resultSet.getBigDecimal("itemId"));
                    items.add(item);
                }
                return items;
            }
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }

        return items;
    }

    private Order initOrder(ResultSet resultSet) throws SQLException {
        Order order = new Order();
        order.setOrderId(resultSet.getBigDecimal("orderId"));
        order.setCountItems(resultSet.getInt("countItems"));
        order.setSum(resultSet.getBigDecimal("sum"));
        order.setStatus(resultSet.getString("status"));
        order.setOpenDate(resultSet.getTimestamp("openDate").toLocalDateTime());
        LocalDateTime executeDate = resultSet.getTimestamp("executeDate") == null ?
                null : resultSet.getTimestamp("executeDate").toLocalDateTime();
        order.setExecuteDate(executeDate);
        order.setClientId(resultSet.getBigDecimal("clientId"));
        order.setItems(getItemsFromOrder(order.getOrderId()));

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
