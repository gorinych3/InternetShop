package ru.gorinych3.inetshop.connectionmanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Применил паттерн Singleton
 */
public class ConnectionManagerJdbcImpl implements ConnectionManager {

    public static final ConnectionManager INSTANCE = new ConnectionManagerJdbcImpl();

    private ConnectionManagerJdbcImpl() {
    }

    public static ConnectionManager getInstance() {
        return INSTANCE;
    }

    @Override
    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/innoInetShop",
                    "postgres",
                    "sa");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
