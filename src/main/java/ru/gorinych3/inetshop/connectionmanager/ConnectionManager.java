package ru.gorinych3.inetshop.connectionmanager;

import java.sql.Connection;

public interface ConnectionManager {

    Connection getConnection();
}
