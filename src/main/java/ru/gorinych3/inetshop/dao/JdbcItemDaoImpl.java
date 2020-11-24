package ru.gorinych3.inetshop.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.MDC;
import ru.gorinych3.inetshop.connectionmanager.ConnectionManagerJdbcImpl;
import ru.gorinych3.inetshop.dto.Item;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("SqlResolve")
public class JdbcItemDaoImpl implements JdbcItemDao {

    private static final Logger LOGGER = LogManager.getLogger(JdbcItemDaoImpl.class.getName());

    @Override
    public List<Item> getAllItems() {

        List<Item> items = new ArrayList<>();
        Item item;

        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT * FROM items")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    item = initItem(resultSet);
                    items.add(item);
                }
            }
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
        return items;
    }


    @Override
    public Item getItemById(BigDecimal itemId) {

        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT * FROM items where itemId = (?)")) {
            preparedStatement.setLong(1, itemId.longValue());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return initItem(resultSet);
            }
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
        return new Item();
    }


    @Override
    public Item getItemByName(String itemName) {

        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT * FROM items where itemName = (?)")) {
            preparedStatement.setString(1, itemName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return initItem(resultSet);
            }
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
        return new Item();
    }

    @Override
    public Item addItem(Item item) {

        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO items values (DEFAULT, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, item.getItemName());
            preparedStatement.setString(2, item.getDescription());
            preparedStatement.setString(3, item.getCategory());
            preparedStatement.setString(4, item.getStatus());
            preparedStatement.setBigDecimal(5, item.getCost());
            preparedStatement.setTimestamp(6, Timestamp.valueOf(item.getCreateDate()));
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return getItemById(generatedKeys.getBigDecimal(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return new Item();
    }

    @Override
    public Item updateItem(Item item) {

        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE items SET itemName = (?), description = (?), itemCategory = (?)," +
                             "status = (?), itemCost = (?), sellDate = (?) where itemId = (?)")) {
            preparedStatement.setString(1, item.getItemName());
            preparedStatement.setString(2, item.getDescription());
            preparedStatement.setString(3, item.getCategory());
            preparedStatement.setString(4, item.getStatus());
            preparedStatement.setBigDecimal(5, item.getCost());
            preparedStatement.setTimestamp(
                    6, item.getSellDate() == null ? null : Timestamp.valueOf(item.getSellDate()));
            preparedStatement.setBigDecimal(7, item.getItemId());
            preparedStatement.executeUpdate();

            return getItemById(item.getItemId());
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return new Item();
    }

    @Override
    public boolean deleteItemById(BigDecimal itemId) {

        try (Connection connection = ConnectionManagerJdbcImpl.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "DELETE FROM items where itemId = (?)")) {

            preparedStatement.setBigDecimal(1, itemId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
        return false;
    }

    private Item initItem(ResultSet resultSet) throws SQLException {
        Item item = new Item();
        item.setItemId(resultSet.getBigDecimal("itemId"));
        item.setItemName(resultSet.getString("itemName"));
        item.setDescription(resultSet.getString("description"));
        item.setCategory(resultSet.getString("itemCategory"));
        item.setCost(resultSet.getBigDecimal("itemCost"));
        item.setStatus(resultSet.getString("status"));
        item.setCreateDate(resultSet.getTimestamp("createDate").toLocalDateTime());
        LocalDateTime sellDate = resultSet.getTimestamp("sellDate") == null ?
                null : resultSet.getTimestamp("sellDate").toLocalDateTime();
        item.setSellDate(sellDate);

        return item;
    }
}
