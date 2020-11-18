package ru.gorinych3.inetshop.dao;

import ru.gorinych3.inetshop.dto.Item;
import ru.gorinych3.inetshop.dto.Order;

import java.math.BigDecimal;
import java.util.List;

/**
 * Применил паттерн Фасад
 */
public interface JdbcOrderDao {

    List<Order> getAllOrders();

    List<Order> getOrdersByStatus(String status);

    Order getOrderById(BigDecimal orderId);

    List<Order> getOrderByClientId(BigDecimal clientId);

    Order addOrder(Order order);

    boolean addItem2Order(Item item, BigDecimal orderId);

    Order changeOrder(Order order);

    boolean deleteOrder(BigDecimal orderId);

    boolean deleteItemFromOrder(BigDecimal orderId, BigDecimal itemId);
}
