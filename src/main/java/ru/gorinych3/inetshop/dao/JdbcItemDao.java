package ru.gorinych3.inetshop.dao;

import ru.gorinych3.inetshop.dto.Item;

import java.math.BigDecimal;
import java.util.List;

/**
 * Применил паттерн Фасад
 */
public interface JdbcItemDao {

    List<Item> getAllItems();

    Item getItemById(BigDecimal itemId);

    Item getItemByName(String itemName);

    Item addItem(Item item);

    Item updateItem(Item item);

    boolean deleteItemById(BigDecimal itemId);
}
