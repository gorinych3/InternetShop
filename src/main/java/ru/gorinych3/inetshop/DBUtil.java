package ru.gorinych3.inetshop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {

    private DBUtil() {
    }

    public static void initDatabase() throws SQLException {

        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/innoInetShop",
                "postgres", "sa");
             Statement statement = connection.createStatement();
        ) {
            statement.execute("-- Database: jdbcDB\n"
                    + "DROP TABLE IF EXISTS clients;"
                    + "CREATE TABLE clients (\n"
                    + "clientId bigserial not null constraint clients_pk primary key,"
                    + "firstName varchar(50)  not null,"
                    + "lastName varchar(100) not null,"
                    + "middleName varchar(100),"
                    + "address varchar(500),"
                    + "phoneNumber varchar(12)  not null,"
                    + "registrationDate timestamp    not null);"
                    + "\n"
                    + "INSERT INTO clients (firstName, lastName, middleName, address, phoneNumber, registrationDate)\n"
                    + "VALUES ('Vasiliy', 'Nikolaev', null, 'Tyumen', '71111111111', LOCALTIMESTAMP),\n"
                    + "   ('Alex', 'Pirogoff', null, 'Tyumen', '72222222222', LOCALTIMESTAMP);\n"
                    + "\n"
                    + "DROP TABLE IF EXISTS items;"
                    + "CREATE TABLE items (\n"
                    + "itemId bigserial not null constraint items_pk primary key,"
                    + "itemName varchar(100) not null,"
                    + "description    varchar(2000),"
                    + "itemCategory varchar(100),"
                    + "status varchar(2) default 'N' not null,"
                    + "itemCost numeric(15, 2) not null,"
                    + "createDate timestamp not null,"
                    + "sellDate timestamp);"
                    + "\n"
                    + "INSERT INTO items (itemName, description, itemCategory, status, itemCost, createDate, sellDate)\n"
                    + "VALUES\n"
                    + "   ('PS4', 'Play Station 4 - 001', 'tech', 'N', 23000.00, LOCALTIMESTAMP, null),\n"
                    + "   ('Notebook', 'HP-17', 'tech', 'N', 43000.00, LOCALTIMESTAMP, null),\n"
                    + "   ('TV', 'LG 46-003', 'tech', 'N', 59000.00, LOCALTIMESTAMP, null),\n"
                    + "   ('Monitor', 'LG 226', 'tech', 'N', 21000.00, LOCALTIMESTAMP, null);\n"
                    + "\n"
                    + "DROP TABLE IF EXISTS admins;"
                    + "CREATE TABLE admins (\n"
                    + "adminId bigserial not null constraint admins_pk primary key,"
                    + "adminName varchar(100) not null,"
                    + "login varchar(150) not null,"
                    + "password    varchar(150) not null);"
                    + "\n"
                    + "INSERT INTO admins (adminName, login, password)\n"
                    + "VALUES\n"
                    + "   ('gorinych', 'gorinych', 'g0r1Nych');\n"
                    + "\n"
                    + "DROP TABLE IF EXISTS orders;"
                    + "CREATE TABLE orders (\n"
                    + "orderId bigserial not null constraint orders_pk primary key,"
                    + "countItems integer,"
                    + "sum numeric(15, 2) default 0.00 not null,"
                    + "status varchar(2) default 'N' not null,"
                    + "openDate timestamp not null,"
                    + "executeDate timestamp,"
                    + "clientId bigint not null);"
                    + "\n"
                    + "DROP TABLE IF EXISTS users;"
                    + "CREATE TABLE users (\n"
                    + "userId bigserial not null constraint users_pk primary key,"
                    + "login varchar(150) not null,"
                    + "password   varchar(150) not null,"
                    + "clientId integer not null);"
                    + "\n"
                    + "DROP TABLE IF EXISTS orderitems;"
                    + "CREATE TABLE orderitems (\n"
                    + "id bigserial not null constraint orderitems_pk primary key,"
                    + "orderId bigint not null,"
                    + "itemId bigint not null);"
                    + "\n"
                    + "DROP TABLE IF EXISTS app_logs;"
                    + "create table APP_LOGS(\n"
                    + "LOG_ID varchar(100) primary key,\n"
                    + "INSERT_DATE timestamp,\n"
                    + "LOGGER varchar(100),\n"
                    + "LOG_LEVEL varchar(30),\n"
                    + "MESSAGE TEXT,\n"
                    + "EXCEPTION TEXT);"
                    + "\n");

        }
    }
}
