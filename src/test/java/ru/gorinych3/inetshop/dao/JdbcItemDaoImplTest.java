package ru.gorinych3.inetshop.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.gorinych3.inetshop.connectionmanager.ConnectionManager;
import ru.gorinych3.inetshop.connectionmanager.ConnectionManagerJdbcImpl;
import ru.gorinych3.inetshop.dto.Item;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class JdbcItemDaoImplTest {

    private JdbcItemDao jdbcItemDao;
    private ConnectionManager connectionManager;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        initMocks(this);
        connection = mock(Connection.class);
        connectionManager = spy(ConnectionManagerJdbcImpl.getInstance());
        doReturn(connection).when(connectionManager).getConnection();
        jdbcItemDao = new JdbcItemDaoImpl(connectionManager);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getAllItems() throws SQLException {
        List<Item> expectedItems = new ArrayList<>();
        when(connection.prepareStatement(JdbcItemDaoImpl.SELECT_ALL_FROM_ITEMS)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        Item expectedItem = initItem();
        expectedItems.add(expectedItem);

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

        List<Item> resultItemList = jdbcItemDao.getAllItems();

        assertEquals(expectedItems.size(), resultItemList.size());
        assertEquals(expectedItems.get(0), resultItemList.get(0));
    }

    @Test
    void getItemById() throws SQLException {
        Item expectedItem = initItem();

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

        assertEquals(expectedItem, jdbcItemDao.getItemById(new BigDecimal("100")));
    }

    @Test
    void getItemByName() throws SQLException {
        Item expectedItem = initItem();

        when(connection.prepareStatement(JdbcItemDaoImpl.SELECT_FROM_ITEMS_BY_NAME)).thenReturn(preparedStatement);
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

        assertEquals(expectedItem, jdbcItemDao.getItemByName("tovar"));
    }

    @Test
    void addItem() throws SQLException {
        Item expectedItem = initItem();
        expectedItem.setSellDate(null);
        when(connection.prepareStatement(JdbcItemDaoImpl.INSERT_INTO_ITEMS, Statement.RETURN_GENERATED_KEYS))
                .thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("100"));

        when(connection.prepareStatement(JdbcItemDaoImpl.SELECT_FROM_ITEMS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("100"));

        when(resultSet.getBigDecimal("itemId")).thenReturn(new BigDecimal("100"));
        when(resultSet.getString("itemName")).thenReturn("tovar");
        when(resultSet.getString("description")).thenReturn("description");
        when(resultSet.getString("itemCategory")).thenReturn("home");
        when(resultSet.getBigDecimal("itemCost")).thenReturn(new BigDecimal("150.00"));
        when(resultSet.getString("status")).thenReturn("O");
        when(resultSet.getTimestamp("createDate"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.parse("2020-11-20T16:30:17.759")));

        Item resultItem = initItem();
        resultItem.setItemId(null);
        resultItem.setSellDate(null);


        assertEquals(expectedItem.getItemId(), jdbcItemDao.addItem(resultItem).getItemId());
    }

    @Test
    void updateItem() throws SQLException {
        Item expectedItem = initItem();
        expectedItem.setSellDate(null);
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

        Item resultItem = initItem();
        resultItem.setItemId(null);
        resultItem.setSellDate(null);

        assertEquals(expectedItem, jdbcItemDao.updateItem(resultItem));
    }

    @Test
    void deleteItemById() throws SQLException {
        when(connection.prepareStatement(JdbcItemDaoImpl.DELETE_FROM_ITEMS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertTrue(jdbcItemDao.deleteItemById(new BigDecimal("100")));
    }

    private Item initItem() {
        Item item = new Item();
        item.setItemId(new BigDecimal("100"));
        item.setItemName("tovar");
        item.setDescription("description");
        item.setCategory("home");
        item.setStatus("O");
        item.setCost(new BigDecimal("150.00"));
        item.setCreateDate(LocalDateTime.parse("2020-11-20T16:30:17.759"));
        item.setSellDate(LocalDateTime.parse("2020-11-28T16:30:17.759"));

        return item;
    }
}