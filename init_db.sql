create table APP_LOGS
(
    LOG_ID      varchar(100) primary key,
    INSERT_DATE timestamp,
    LOGGER      varchar(100),
    LOG_LEVEL   varchar(30),
    MESSAGE     TEXT,
    EXCEPTION   TEXT
);

CREATE TABLE clients
(
    clientId bigserial not null constraint clients_pk primary key,
    firstName        varchar(50)  not null,
    lastName         varchar(100) not null,
    middleName       varchar(100),
    address          varchar(500),
    phoneNumber      varchar(12)  not null,
    registrationDate timestamp    not null
);

CREATE TABLE items
(
    itemId bigserial not null constraint items_pk primary key,
    itemName     varchar(100)           not null,
    description  varchar(2000),
    itemCategory varchar(100),
    status       varchar(2) default 'N' not null,
    itemCost     numeric(15, 2)         not null,
    createDate   timestamp              not null,
    sellDate     timestamp
);

CREATE TABLE admins
(
    adminId bigserial not null constraint admins_pk primary key,
    adminName varchar(100) not null,
    login     varchar(150) not null,
    password  varchar(150) not null
);

CREATE TABLE orders
(
    orderId bigserial not null constraint orders_pk primary key,
    countItems  integer,
    sum         numeric(15, 2) default 0.00 not null,
    status      varchar(2)     default 'N'  not null,
    openDate    timestamp                   not null,
    executeDate timestamp,
    clientId    bigint                      not null
);

CREATE TABLE users
(
    userId bigserial not null constraint users_pk primary key,
    login    varchar(150) not null,
    password varchar(150) not null,
    clientId integer      not null
);

CREATE TABLE orderitems
(
    id bigserial not null constraint orderitems_pk primary key,
    orderId bigint not null,
    itemId  bigint not null
);


INSERT INTO clients (firstName, lastName, middleName, address, phoneNumber, registrationDate)
VALUES ('Vasiliy', 'Nikolaev', null, 'Tyumen', '71111111111', LOCALTIMESTAMP),
       ('Alex', 'Pirogoff', null, 'Tyumen', '72222222222', LOCALTIMESTAMP);

INSERT INTO items (itemName, description, itemCategory, status, itemCost, createDate, sellDate)
VALUES ('PS4', 'Play Station 4 - 001', 'tech', 'N', 23000.00, LOCALTIMESTAMP, null),
       ('Notebook', 'HP-17', 'tech', 'N', 43000.00, LOCALTIMESTAMP, null),
       ('TV', 'LG 46-003', 'tech', 'N', 59000.00, LOCALTIMESTAMP, null),
       ('Monitor', 'LG 226', 'tech', 'N', 21000.00, LOCALTIMESTAMP, null);

INSERT INTO admins (adminName, login, password)
VALUES ('gorinych', 'gorinych', 'g0r1Nych');