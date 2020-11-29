package ru.gorinych3.inetshop.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.gorinych3.inetshop.connectionmanager.ConnectionManager;
import ru.gorinych3.inetshop.connectionmanager.ConnectionManagerJdbcImpl;
import ru.gorinych3.inetshop.dto.Client;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class JdbcServiceDaoImplTest {

    private JdbcServiceDao jdbcServiceDao;
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
        jdbcServiceDao = spy(new JdbcServiceDaoImpl(connectionManager));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void cleanOldOrdersTrue() throws SQLException {
        when(connection.prepareStatement(JdbcServiceDaoImpl.DELETE_FROM_ORDERS)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(5);
        assertTrue(jdbcServiceDao.cleanOldOrders());
    }

    @Test
    void cleanOldOrdersFalse() throws SQLException {
        when(connection.prepareStatement(JdbcServiceDaoImpl.DELETE_FROM_ORDERS)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);
        assertFalse(jdbcServiceDao.cleanOldOrders());
    }

    @Test
    void getAllClients() throws SQLException {
        List<Client> expectedClientList = new ArrayList<>();
        when(connection.prepareStatement(JdbcServiceDaoImpl.SELECT_ALL_FROM_CLIENTS)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Client expectedClient = initClient();
        expectedClientList.add(expectedClient);

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getBigDecimal("clientId")).thenReturn(expectedClient.getClientId());
        when(resultSet.getString("firstName")).thenReturn(expectedClient.getFirstName());
        when(resultSet.getString("lastName")).thenReturn(expectedClient.getLastName());
        when(resultSet.getString("middleName")).thenReturn(expectedClient.getMiddleName());
        when(resultSet.getString("address")).thenReturn(expectedClient.getAddress());
        when(resultSet.getString("phoneNumber")).thenReturn(expectedClient.getPhoneNumber());
        when(resultSet.getTimestamp("registrationDate"))
                .thenReturn(Timestamp.valueOf(expectedClient.getRegistrationDate()));


        List<Client> resultList = jdbcServiceDao.getAllClients();

        assertEquals(expectedClientList.size(), resultList.size());
        assertEquals(expectedClientList.get(0), resultList.get(0));
        verify(connection, times(1)).prepareStatement(
                JdbcServiceDaoImpl.SELECT_ALL_FROM_CLIENTS);
        verify(preparedStatement, atMost(1)).executeQuery();
    }

    @Test
    void addNewClientOk() throws SQLException {
        when(connection.prepareStatement(JdbcServiceDaoImpl.INSERT_INTO_CLIENTS, Statement.RETURN_GENERATED_KEYS))
                .thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));

        Client newClient = new Client();
        newClient.setFirstName("Ivan");
        newClient.setLastName("Sidorov");
        newClient.setAddress("Tyumen");
        newClient.setPhoneNumber("79876543221");
        newClient.setRegistrationDate(LocalDateTime.now());

        final BigDecimal id = jdbcServiceDao.addNewClient(newClient).getClientId();

        assertEquals(new BigDecimal("1"), id);
        verify(connection, times(1)).prepareStatement(
                JdbcServiceDaoImpl.INSERT_INTO_CLIENTS, Statement.RETURN_GENERATED_KEYS);
        verify(preparedStatement, atMost(1)).executeUpdate();
    }

    @Test
    void addNewClientEmpty() throws SQLException {
        when(connection.prepareStatement(JdbcServiceDaoImpl.INSERT_INTO_CLIENTS, Statement.RETURN_GENERATED_KEYS))
                .thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));

        Client newClient = new Client();
        Client client = jdbcServiceDao.addNewClient(newClient);

        assertNull(client.getClientId());
        verify(connection, times(1)).prepareStatement(
                JdbcServiceDaoImpl.INSERT_INTO_CLIENTS, Statement.RETURN_GENERATED_KEYS);
        verify(preparedStatement, atMost(1)).executeUpdate();
    }

    @Test
    void getClientById() throws SQLException {
        when(connection.prepareStatement(JdbcServiceDaoImpl.SELECT_FROM_CLIENTS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Client expectedClient = initClient();
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBigDecimal("clientId")).thenReturn(expectedClient.getClientId());
        when(resultSet.getString("firstName")).thenReturn(expectedClient.getFirstName());
        when(resultSet.getString("lastName")).thenReturn(expectedClient.getLastName());
        when(resultSet.getString("middleName")).thenReturn(expectedClient.getMiddleName());
        when(resultSet.getString("address")).thenReturn(expectedClient.getAddress());
        when(resultSet.getString("phoneNumber")).thenReturn(expectedClient.getPhoneNumber());
        when(resultSet.getTimestamp("registrationDate"))
                .thenReturn(Timestamp.valueOf(expectedClient.getRegistrationDate()));

        assertEquals(expectedClient, jdbcServiceDao.getClientById(new BigDecimal("1")));
        verify(connection, times(1)).prepareStatement(
                JdbcServiceDaoImpl.SELECT_FROM_CLIENTS_BY_ID);
        verify(preparedStatement, atMost(1)).executeUpdate();
    }

    @Test
    void deleteClientTrue() throws SQLException {
        when(connection.prepareStatement(JdbcServiceDaoImpl.DELETE_FROM_CLIENTS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        assertTrue(jdbcServiceDao.deleteClient(new BigDecimal("1")));
    }

    @Test
    void deleteClientFalse() throws SQLException {
        when(connection.prepareStatement(JdbcServiceDaoImpl.DELETE_FROM_CLIENTS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);
        assertFalse(jdbcServiceDao.deleteClient(new BigDecimal("1")));
    }

    @Test
    void registrationUser() throws SQLException {
        when(connection.prepareStatement(JdbcServiceDaoImpl.INSERT_INTO_USERS)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        assertTrue(jdbcServiceDao.registrationUser("login", "password", new BigDecimal("1")));
    }

    @Test
    void checkUserRegistrationDataTrue() throws SQLException {
        when(connection.prepareStatement(JdbcServiceDaoImpl.SELECT_FROM_USERS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("login")).thenReturn("login1");
        when(resultSet.getString("password")).thenReturn("password1");
        assertTrue(jdbcServiceDao.checkUserRegistrationData(
                new BigDecimal("1"), "login1", "password1"));
    }

    @Test
    void checkUserRegistrationDataFalse() throws SQLException {
        when(connection.prepareStatement(JdbcServiceDaoImpl.SELECT_FROM_USERS_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("login")).thenReturn("login2");
        when(resultSet.getString("password")).thenReturn("password2");
        assertFalse(jdbcServiceDao.checkUserRegistrationData(
                new BigDecimal("1"), "login1", "password1"));
    }

    @Test
    void changeUserPassword() throws SQLException {
        when(connection.prepareStatement(JdbcServiceDaoImpl.UPDATE_USERS_PASSWORD_BY_ID)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        assertTrue(jdbcServiceDao.changeUserPassword(new BigDecimal("1"), "password"));
    }

    private Client initClient() {
        Client newClient = new Client();
        newClient.setClientId(new BigDecimal("1"));
        newClient.setFirstName("Ivan");
        newClient.setLastName("Sidorov");
        newClient.setAddress("Tyumen");
        newClient.setPhoneNumber("79876543221");
        newClient.setRegistrationDate(LocalDateTime.parse("2020-11-28T16:30:17.759"));

        return newClient;
    }
}