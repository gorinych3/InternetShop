package ru.gorinych3.inetshop.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.gorinych3.inetshop.connectionmanager.ConnectionManager;
import ru.gorinych3.inetshop.connectionmanager.ConnectionManagerJdbcImpl;
import ru.gorinych3.inetshop.dto.Item;
import ru.gorinych3.inetshop.dto.Order;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class JdbcOrderDaoImplTest {

    private static final Logger LOGGER = LogManager.getLogger(JdbcOrderDaoImplTest.class);

    private JdbcOrderDao jdbcOrderDao;
    private JdbcItemDao jdbcItemDao;
    private ConnectionManager connectionManager;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;


    @BeforeEach
    void setUp() throws SQLException, IllegalAccessException, InstantiationException {
        initMocks(this);
        connection = mock(Connection.class);

        initMocks(this);
        connection = mock(Connection.class);
        connectionManager = spy(ConnectionManagerJdbcImpl.getInstance());
        doReturn(connection).when(connectionManager).getConnection();
        jdbcItemDao = spy(new JdbcItemDaoImpl(connectionManager));
        jdbcOrderDao = spy(new JdbcOrderDaoImpl(jdbcItemDao, connectionManager));

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getAllOrders() throws SQLException {
        List<Order> expectedOrderList = new ArrayList<>();
        Order order = initOrder();
        expectedOrderList.add(order);
        when(connection.prepareStatement(JdbcOrderDaoImpl.SELECT_ALL_FROM_ORDERS)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getBigDecimal("orderId")).thenReturn(new BigDecimal("2"));
        when(resultSet.getInt("countItems")).thenReturn(1);
        when(resultSet.getBigDecimal("sum")).thenReturn(new BigDecimal("10.0"));
        when(resultSet.getString("status")).thenReturn("N");
        when(resultSet.getTimestamp("openDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-20T16:30:17.759")));
        when(resultSet.getTimestamp("executeDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-28T16:30:17.759")));
        when(resultSet.getBigDecimal("clientId")).thenReturn(new BigDecimal("2"));

        when(connection.prepareStatement(JdbcOrderDaoImpl.SELECT_ALL_FROM_ORDERITEMS_BY_ORDER_ID))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);


        List<Order> resultOrderList = jdbcOrderDao.getAllOrders();
        LOGGER.info(resultOrderList.get(0));
        assertEquals(expectedOrderList.size(), resultOrderList.size());
        assertEquals(expectedOrderList.get(0), resultOrderList.get(0));

    }

    @Test
    void getOrdersByStatus() throws SQLException {
        List<Order> expectedOrderList = new ArrayList<>();
        Order expectedOrder = initOrder();
        expectedOrder.setStatus("E");
        expectedOrderList.add(expectedOrder);
        when(connection.prepareStatement(JdbcOrderDaoImpl.SELECT_ALL_FROM_ORDERS_BY_STATUS))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getBigDecimal("orderId")).thenReturn(new BigDecimal("2"));
        when(resultSet.getInt("countItems")).thenReturn(1);
        when(resultSet.getBigDecimal("sum")).thenReturn(new BigDecimal("10.0"));
        when(resultSet.getString("status")).thenReturn("E");
        when(resultSet.getTimestamp("openDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-20T16:30:17.759")));
        when(resultSet.getTimestamp("executeDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-28T16:30:17.759")));
        when(resultSet.getBigDecimal("clientId")).thenReturn(new BigDecimal("2"));

        when(connection.prepareStatement(JdbcOrderDaoImpl.SELECT_ALL_FROM_ORDERITEMS_BY_ORDER_ID))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        List<Order> resultOrderList = jdbcOrderDao.getOrdersByStatus("E");

        assertEquals(expectedOrderList.size(), resultOrderList.size());
        assertEquals(expectedOrderList.get(0), resultOrderList.get(0));
    }

    @Test
    void getOrderById() throws SQLException {
        Order expectedOrder = initOrder();
        when(connection.prepareStatement(JdbcOrderDaoImpl.SELECT_FROM_ORDERS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);

        when(resultSet.getBigDecimal("orderId")).thenReturn(new BigDecimal("2"));
        when(resultSet.getInt("countItems")).thenReturn(1);
        when(resultSet.getBigDecimal("sum")).thenReturn(new BigDecimal("10.0"));
        when(resultSet.getString("status")).thenReturn("N");
        when(resultSet.getTimestamp("openDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-20T16:30:17.759")));
        when(resultSet.getTimestamp("executeDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-28T16:30:17.759")));
        when(resultSet.getBigDecimal("clientId")).thenReturn(new BigDecimal("2"));


        when(connection.prepareStatement(JdbcOrderDaoImpl.SELECT_ALL_FROM_ORDERITEMS_BY_ORDER_ID))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(connection.prepareStatement(JdbcItemDaoImpl.SELECT_FROM_ITEMS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertEquals(expectedOrder, jdbcOrderDao.getOrderById(new BigDecimal("2")));
    }

    @Test
    void getOrderByClientId() throws SQLException {
        List<Order> expectedOrderList = new ArrayList<>();
        Order expectedOrder = initOrder();
        expectedOrder.setStatus("O");
        expectedOrder.setClientId(new BigDecimal("1"));
        expectedOrderList.add(expectedOrder);
        when(connection.prepareStatement(JdbcOrderDaoImpl.SELECT_ALL_FROM_ORDERS_BY_CLIENT_ID))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getBigDecimal("orderId")).thenReturn(new BigDecimal("2"));
        when(resultSet.getInt("countItems")).thenReturn(1);
        when(resultSet.getBigDecimal("sum")).thenReturn(new BigDecimal("10.0"));
        when(resultSet.getString("status")).thenReturn("O");
        when(resultSet.getTimestamp("openDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-20T16:30:17.759")));
        when(resultSet.getTimestamp("executeDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-28T16:30:17.759")));
        when(resultSet.getBigDecimal("clientId")).thenReturn(new BigDecimal("1"));

        when(connection.prepareStatement(JdbcOrderDaoImpl.SELECT_ALL_FROM_ORDERITEMS_BY_ORDER_ID))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        List<Order> resultOrderList = jdbcOrderDao.getOrderByClientId(new BigDecimal("2"));

        assertEquals(expectedOrderList.size(), resultOrderList.size());
        assertEquals(expectedOrderList.get(0), resultOrderList.get(0));
    }

    @Test
    void addOrder() throws SQLException {
        Order expectedOrder = initOrder();

        when(connection.prepareStatement(JdbcOrderDaoImpl.INSERT_INTO_ORDERS, Statement.RETURN_GENERATED_KEYS))
                .thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("2"));

        when(connection.prepareStatement(JdbcOrderDaoImpl.INSERT_INTO_ORDERITEMS)).thenReturn(preparedStatement);
        when(preparedStatement.executeBatch()).thenReturn(new int[]{});

        when(connection.prepareStatement(JdbcOrderDaoImpl.SELECT_FROM_ORDERS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.getBigDecimal("orderId")).thenReturn(new BigDecimal("2"));
        when(resultSet.getInt("countItems")).thenReturn(1);
        when(resultSet.getBigDecimal("sum")).thenReturn(new BigDecimal("10.0"));
        when(resultSet.getString("status")).thenReturn("N");
        when(resultSet.getTimestamp("openDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-20T16:30:17.759")));
        when(resultSet.getTimestamp("executeDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-28T16:30:17.759")));
        when(resultSet.getBigDecimal("clientId")).thenReturn(new BigDecimal("2"));

        when(connection.prepareStatement(JdbcOrderDaoImpl.SELECT_ALL_FROM_ORDERITEMS_BY_ORDER_ID))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(connection.prepareStatement(JdbcItemDaoImpl.SELECT_FROM_ITEMS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        assertEquals(expectedOrder.getOrderId(), jdbcOrderDao.addOrder(expectedOrder).getOrderId());
    }

    @Test
    void addItem2Order() throws SQLException {
        Item expectedItem = initItem();
        Order expectedOrder = initOrder();

        when(connection.prepareStatement(JdbcOrderDaoImpl.INSERT_INTO_ORDERITEMS)).thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(true);

        expectedItem.setItemName("НОВЫЙ ТОВАР");
        when(connection.prepareStatement(JdbcItemDaoImpl.UPDATE_ITEMS)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        when(connection.prepareStatement(JdbcItemDaoImpl.SELECT_FROM_ITEMS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        when(resultSet.getBigDecimal("itemId")).thenReturn(new BigDecimal("100"));
        when(resultSet.getString("itemName")).thenReturn("НОВЫЙ ТОВАР");
        when(resultSet.getString("description")).thenReturn("description");
        when(resultSet.getString("itemCategory")).thenReturn("home");
        when(resultSet.getBigDecimal("itemCost")).thenReturn(new BigDecimal("150.00"));
        when(resultSet.getString("status")).thenReturn("O");
        when(resultSet.getTimestamp("createDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-20T16:30:17.759")));


        assertTrue(jdbcOrderDao.addItem2Order(expectedItem, expectedOrder.getOrderId()));
    }

    @Test
    void deleteItemFromOrder() throws SQLException {
        Item expectedItem = initItem();
        Order expectedOrder = initOrder();

        when(connection.prepareStatement(JdbcOrderDaoImpl.DELETE_FROM_ORDERITEMS)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        when(connection.prepareStatement(JdbcItemDaoImpl.SELECT_FROM_ITEMS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        when(resultSet.getBigDecimal("itemId")).thenReturn(new BigDecimal("100"));
        when(resultSet.getString("itemName")).thenReturn("tovar");
        when(resultSet.getString("description")).thenReturn("description");
        when(resultSet.getString("itemCategory")).thenReturn("home");
        when(resultSet.getBigDecimal("itemCost")).thenReturn(new BigDecimal("150.00"));
        when(resultSet.getString("status")).thenReturn("O");
        when(resultSet.getTimestamp("createDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-20T16:30:17.759")));
        when(resultSet.getTimestamp("sellDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-28T16:30:17.759")));

        when(connection.prepareStatement(JdbcItemDaoImpl.UPDATE_ITEMS)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        when(connection.prepareStatement(JdbcItemDaoImpl.SELECT_FROM_ITEMS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        when(resultSet.getBigDecimal("itemId")).thenReturn(new BigDecimal("100"));
        when(resultSet.getString("itemName")).thenReturn("НОВЫЙ ТОВАР");
        when(resultSet.getString("description")).thenReturn("description");
        when(resultSet.getString("itemCategory")).thenReturn("home");
        when(resultSet.getBigDecimal("itemCost")).thenReturn(new BigDecimal("150.00"));
        when(resultSet.getString("status")).thenReturn("O");
        when(resultSet.getTimestamp("createDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-20T16:30:17.759")));

        assertTrue(jdbcOrderDao.deleteItemFromOrder(expectedOrder.getOrderId(), expectedItem.getItemId()));
    }

    @Test
    void changeOrder() throws SQLException {
        Order expectedOrder = initOrder();
        when(connection.prepareStatement(JdbcOrderDaoImpl.UPDATE_ORDERS)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertEquals(expectedOrder, jdbcOrderDao.changeOrder(expectedOrder));
    }

    @Test
    void deleteOrder() throws SQLException {
        when(connection.prepareStatement(JdbcOrderDaoImpl.DELETE_FROM_ORDER_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        when(connection.prepareStatement(JdbcOrderDaoImpl.DELETE_FROM_ORDERITEMS_BY_ORDER_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(jdbcOrderDao.deleteOrder(new BigDecimal("1")));
    }

    private Order initOrder() {
        List<Item> itemList = new ArrayList<>();

        Order order = new Order();
        order.setOrderId(new BigDecimal("2"));
        order.setCountItems(1);
        order.setSum(new BigDecimal("10.0"));
        order.setStatus("N");
        order.setOpenDate(LocalDateTime.parse("2020-11-20T16:30:17.759"));
        order.setExecuteDate(LocalDateTime.parse("2020-11-28T16:30:17.759"));
        order.setClientId(new BigDecimal("2"));
        order.setItems(itemList);

        return order;
    }

    private Item initItem() {
        Item item = new Item();
        item.setItemId(new BigDecimal("10"));
        item.setItemName("tovar");
        item.setDescription("description");
        item.setCategory("");
        item.setCost(new BigDecimal("10.0"));
        item.setStatus("O");
        item.setCreateDate(LocalDateTime.parse("2020-11-10T08:30:17.759"));

        return item;
    }
}