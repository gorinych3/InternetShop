package ru.gorinych3.inetshop;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ru.gorinych3.inetshop.dao.*;
import ru.gorinych3.inetshop.dto.Client;
import ru.gorinych3.inetshop.dto.Item;
import ru.gorinych3.inetshop.dto.Order;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Задание 1. Взять за основу предметную область выбранную на занятии по UML:
 * <p>
 * Спроектировать базу данных для выбранной предметной области (минимум три таблицы).
 * Типы и состав полей в таблицах на ваше усмотрение.
 * Связи между таблицами делать не обязательно.
 * Задание 2. Через JDBC интерфейс описать CRUD операции с созданными таблицами:
 * <p>
 * Применить параметризованный запрос.
 * Применить батчинг.
 * Использовать ручное управление транзакциями.
 * Предусмотреть использование savepoint при выполнении логики из нескольких запросов.
 * Предусмотреть rollback операций при ошибках.
 * Желательно предусмотреть метод сброса и инициализации базы данных.
 */

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) throws SQLException {

        JdbcItemDao jdbcItemDao = new JdbcItemDaoImpl();
        JdbcOrderDao jdbcOrderDao = new JdbcOrderDaoImpl(jdbcItemDao);
        JdbcServiceDao jdbcServiceDao = new JdbcServiceDaoImpl();

        DBUtil.initDatabase();
        serviceMethod(jdbcServiceDao);
        itemsMethod(jdbcItemDao);
        ordersMethod(jdbcOrderDao, jdbcItemDao, jdbcServiceDao);

        LOGGER.info(jdbcServiceDao.cleanOldOrders());

    }

    public static void serviceMethod(JdbcServiceDao jdbcServiceDao) {
        Client newClient = new Client();
        newClient.setFirstName("Ivan");
        newClient.setLastName("Sidorov");
        newClient.setAddress("Tyumen");
        newClient.setPhoneNumber("79876543221");
        newClient.setRegistrationDate(LocalDateTime.now());

        Client client = jdbcServiceDao.addNewClient(newClient, "login1", "password1");
        LOGGER.info("Client client =" + client.toString());
        LOGGER.info("=========================================================================" + "\n");

        printList(jdbcServiceDao.getAllClients());

        LOGGER.info("changeUserPassword: "
                + jdbcServiceDao.changeUserPassword(client.getClientId(), "changedPassword"));
        LOGGER.info("=========================================================================" + "\n");

        LOGGER.info("checkUserRegistrationData: "
                + jdbcServiceDao.checkUserRegistrationData(client.getClientId(), "login1", "password1"));
        LOGGER.info("checkUserRegistrationData: "
                + jdbcServiceDao.checkUserRegistrationData(client.getClientId(), "login1", "changedPassword"));
        LOGGER.info("=========================================================================" + "\n");

        Client clientByIdTrue = jdbcServiceDao.getClientById(new BigDecimal("2"));
        LOGGER.info("clientByIdTrue: " + clientByIdTrue.toString());
        Client clientByIdFalse = jdbcServiceDao.getClientById(new BigDecimal("789"));
        LOGGER.info("clientByIdFalse: " + clientByIdFalse.toString());
        LOGGER.info("=========================================================================" + "\n");

        LOGGER.info("deleteClient: " + jdbcServiceDao.deleteClient(new BigDecimal("156")));
        LOGGER.info("deleteClient: " + jdbcServiceDao.deleteClient(new BigDecimal("2")));
        LOGGER.info("=========================================================================" + "\n");

        LOGGER.info("cleanOldOrders: " + jdbcServiceDao.cleanOldOrders());
        LOGGER.info("=========================================================================" + "\n");
    }

    public static void ordersMethod(JdbcOrderDao jdbcOrderDao, JdbcItemDao jdbcItemDao, JdbcServiceDao jdbcServiceDao) {

        Client client = jdbcServiceDao.getClientById(new BigDecimal("3"));

        List<Item> items = jdbcItemDao.getAllItems();
        //printList(items);

        Order order = new Order();
        order.setStatus("N");
        order.setOpenDate(LocalDateTime.now());
        order.setClientId(client.getClientId());
        order.setItems(items);

        LOGGER.info("addOrder" + jdbcOrderDao.addOrder(order));
        LOGGER.info("=========================================================================" + "\n");

        printList(jdbcOrderDao.getAllOrders());

        LOGGER.info("getOrdersByStatus");
        printList(jdbcOrderDao.getOrdersByStatus("N"));

        order.setStatus("E");
        LOGGER.info("changeOrder");
        jdbcOrderDao.changeOrder(order);

        printList(jdbcOrderDao.getAllOrders());

        jdbcItemDao.addItem(new Item(
                "Микроволновка",
                "обычная, без наворотов",
                "кухонная утварь",
                new BigDecimal("8500.00"),
                LocalDateTime.now(),
                null,
                "N"));

        jdbcOrderDao.addItem2Order(jdbcItemDao.getItemById(new BigDecimal("5")), order.getOrderId());

        LOGGER.info("Order before adding new item " + jdbcOrderDao.getOrderById(order.getOrderId()));
        LOGGER.info("=========================================================================" + "\n");


        boolean resDeleteItem = jdbcOrderDao.deleteItemFromOrder(order.getOrderId(), new BigDecimal("3"));
        LOGGER.info("deleteItemFromOrder = " + resDeleteItem + "    " + jdbcOrderDao.getOrderById(order.getOrderId()));
        LOGGER.info("=========================================================================" + "\n");

        printList(jdbcOrderDao.getOrderByClientId(new BigDecimal("3")));
        LOGGER.info("=========================================================================" + "\n");

        LOGGER.info("deleteOrder");
        jdbcOrderDao.deleteOrder(order.getOrderId());
        printList(jdbcOrderDao.getAllOrders());

    }

    public static void itemsMethod(JdbcItemDao jdbcItemDao) {
        printList(jdbcItemDao.getAllItems());

        LOGGER.info("getItemById: " + jdbcItemDao.getItemById(new BigDecimal(String.valueOf(2))));
        LOGGER.info("=========================================================================" + "\n");

        LOGGER.info("addItem: " + jdbcItemDao.addItem(new Item(
                "notepad",
                "чо там описывать?",
                "tech",
                new BigDecimal(String.valueOf(15000.00)),
                LocalDateTime.now(),
                null,
                "N"
        )));
        LOGGER.info("=========================================================================" + "\n");
        LOGGER.info("updateItem: " + jdbcItemDao.updateItem(new Item(
                new BigDecimal(String.valueOf(2)),
                "notepad",
                "чо там описывать?",
                "tech",
                new BigDecimal(String.valueOf(15000.00)),
                null,
                LocalDateTime.now(),
                "S"
        )));
        LOGGER.info("=========================================================================" + "\n");

        LOGGER.info("deleteItemById: " + jdbcItemDao.deleteItemById(new BigDecimal(String.valueOf(2))));
        LOGGER.info("=========================================================================" + "\n");

        printList(jdbcItemDao.getAllItems());
    }

    public static <T> void printList(List<T> objects) {
        LOGGER.info("Show List: ");
        for (T t : objects) {
            LOGGER.info(t.toString());
        }
        LOGGER.info("=========================================================================" + "\n");
    }
}
