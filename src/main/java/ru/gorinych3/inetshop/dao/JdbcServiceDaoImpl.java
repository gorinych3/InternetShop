package ru.gorinych3.inetshop.dao;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gorinych3.inetshop.connectionmanager.ConnectionManager;
import ru.gorinych3.inetshop.dto.Client;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("SqlResolve")
public class JdbcServiceDaoImpl implements JdbcServiceDao {

    public static final String DELETE_FROM_ORDERS = "DELETE FROM orders where status = 'E' " +
            "and (EXTRACT(EPOCH FROM executeDate) - EXTRACT(EPOCH FROM LOCALTIMESTAMP))/60/60/24 > 30;";
    public static final String SELECT_ALL_FROM_CLIENTS = "SELECT * FROM clients";
    public static final String INSERT_INTO_CLIENTS = "INSERT INTO clients VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)";
    public static final String SELECT_FROM_CLIENTS_BY_ID = "SELECT * FROM clients where clientId = (?)";
    public static final String DELETE_FROM_CLIENTS_BY_ID = "DELETE FROM clients where clientId = (?)";
    public static final String INSERT_INTO_USERS = "INSERT INTO users values (DEFAULT, ?, ?, ?)";
    public static final String SELECT_FROM_USERS_BY_ID = "SELECT * FROM users where clientId = (?)";
    public static final String UPDATE_USERS_PASSWORD_BY_ID = "UPDATE users SET password = (?) where clientId = (?)";
    private static final Logger LOGGER = LogManager.getLogger(JdbcServiceDaoImpl.class);
    private final ConnectionManager connectionManager;

    public JdbcServiceDaoImpl(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public boolean cleanOldOrders() {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FROM_ORDERS)) {
            preparedStatement.executeUpdate();
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }

    @Override
    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        Client client;

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_FROM_CLIENTS)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    client = initClient(resultSet);
                    clients.add(client);
                }
            }
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
        return clients;
    }

    @Override
    public Client addNewClient(Client newClient) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     INSERT_INTO_CLIENTS, Statement.RETURN_GENERATED_KEYS)) {

            if (!clientIsEmpty(newClient)) {
                LOGGER.info("условие выполняется");
                preparedStatement.setString(1, newClient.getFirstName());
                preparedStatement.setString(2, newClient.getLastName());
                preparedStatement.setString(3, newClient.getMiddleName());
                preparedStatement.setString(4, newClient.getAddress());
                preparedStatement.setString(5, newClient.getPhoneNumber());
                preparedStatement.setTimestamp(6, Timestamp.valueOf(newClient.getRegistrationDate()));
                preparedStatement.executeUpdate();

                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newClient.setClientId(generatedKeys.getBigDecimal(1));
                    }
                }
                return newClient;
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return new Client();
    }

    @Override
    public Client getClientById(BigDecimal clientId) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     SELECT_FROM_CLIENTS_BY_ID)) {
            preparedStatement.setLong(1, clientId.longValue());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return initClient(resultSet);
            }
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
        return new Client();
    }

    @Override
    public boolean deleteClient(BigDecimal clientId) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     DELETE_FROM_CLIENTS_BY_ID)) {

            preparedStatement.setBigDecimal(1, clientId);
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean registrationUser(String login, String password, BigDecimal clientId) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     INSERT_INTO_USERS)) {
            if (!Objects.equals(clientId, null) || !Objects.equals(login, null)
                    || !Objects.equals(password, null)) {
                preparedStatement.setString(1, login);
                preparedStatement.setString(2, password);
                preparedStatement.setBigDecimal(3, clientId);
                return preparedStatement.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean checkUserRegistrationData(BigDecimal clientId, String login, String password) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     SELECT_FROM_USERS_BY_ID)) {
            if (!Objects.equals(clientId, null) || !Objects.equals(login, null)
                    || !Objects.equals(password, null)) {
                preparedStatement.setBigDecimal(1, clientId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    if (Objects.equals(resultSet.getString("login"), login) &&
                            Objects.equals(resultSet.getString("password"), password)) {
                        return true;
                    }
                }
            }
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
        return false;
    }

    @Override
    public boolean changeUserPassword(BigDecimal clientId, String password) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     UPDATE_USERS_PASSWORD_BY_ID)) {
            if (!Objects.equals(password, null)) {
                preparedStatement.setString(1, password);
                preparedStatement.setBigDecimal(2, clientId);
                return preparedStatement.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }

    private Client initClient(ResultSet resultSet) throws SQLException {
        Client client = new Client();
        client.setClientId(resultSet.getBigDecimal("clientId"));
        client.setFirstName(resultSet.getString("firstName"));
        client.setLastName(resultSet.getString("lastName"));
        client.setMiddleName(resultSet.getString("middleName"));
        client.setAddress(resultSet.getString("address"));
        client.setPhoneNumber(resultSet.getString("phoneNumber"));
        client.setRegistrationDate(resultSet.getTimestamp("registrationDate").toLocalDateTime());

        return client;
    }

    private boolean clientIsEmpty(Client client) {
        return client.getFirstName() == null || client.getLastName() == null
                || client.getAddress() == null || client.getPhoneNumber() == null
                || client.getRegistrationDate() == null;
    }
}
