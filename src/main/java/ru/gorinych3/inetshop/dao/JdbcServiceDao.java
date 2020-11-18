package ru.gorinych3.inetshop.dao;

import ru.gorinych3.inetshop.dto.Client;

import java.math.BigDecimal;
import java.util.List;


/**
 * Применил паттерн Фасад
 */
public interface JdbcServiceDao {

    boolean cleanOldOrders();

    List<Client> getAllClients();

    Client addNewClient(Client newClient, String login, String password);

    Client getClientById(BigDecimal clientId);

    boolean deleteClient(BigDecimal clientId);

    boolean checkUserRegistrationData(BigDecimal clientId, String login, String password);

    boolean changeUserPassword(BigDecimal clientId, String password);
}
