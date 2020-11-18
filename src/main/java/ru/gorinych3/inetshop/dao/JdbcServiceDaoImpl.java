package ru.gorinych3.inetshop.dao;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.gorinych3.inetshop.connectionmanager.ConnectionManagerJdbcImpl;
import ru.gorinych3.inetshop.dto.Client;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("SqlResolve")
public class JdbcServiceDaoImpl implements JdbcServiceDao {

    private static final Logger LOGGER = LogManager.getLogger(JdbcServiceDaoImpl.class.getName());

    @Override
    public boolean cleanOldOrders() {
        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "DELETE FROM orders where status = 'E' " +
                             "and (EXTRACT(EPOCH FROM executeDate) - " +
                             "EXTRACT(EPOCH FROM LOCALTIMESTAMP))/60/60/24 > 30;")) {
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }

    @Override
    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        Client client;

        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT * FROM clients")) {
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
    public Client addNewClient(Client newClient, String login, String password) {
        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO clients values (DEFAULT, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

            connection.setAutoCommit(false);
            Savepoint addClient = connection.setSavepoint("add client");

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
                    if (registrationUser(login, password, newClient.getClientId())) {
                        connection.commit();
                        connection.setAutoCommit(true);
                        return newClient;
                    } else {
                        connection.rollback(addClient);
                        connection.setAutoCommit(true);
                        return new Client();
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return new Client();
    }

    @Override
    public Client getClientById(BigDecimal clientId) {
        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT * FROM clients where clientId = (?)")) {
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
        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "DELETE FROM clients where clientId = (?)")) {

            preparedStatement.setBigDecimal(1, clientId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }


    private boolean registrationUser(String login, String password, BigDecimal clientId) {
        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO users values (DEFAULT, ?, ?, ?)")) {
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            preparedStatement.setBigDecimal(3, clientId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean checkUserRegistrationData(BigDecimal clientId, String login, String password) {
        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT * FROM users where clientId = (?)")) {
            preparedStatement.setLong(1, clientId.longValue());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                if (Objects.equals(resultSet.getString("login"), login) &&
                        Objects.equals(resultSet.getString("password"), password)) {
                    return true;
                }
            }
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
        return false;
    }

    @Override
    public boolean changeUserPassword(BigDecimal clientId, String password) {
        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE users SET password = (?) where clientId = (?)")) {
            preparedStatement.setString(1, password);
            preparedStatement.setBigDecimal(2, clientId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
        return true;
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
}
