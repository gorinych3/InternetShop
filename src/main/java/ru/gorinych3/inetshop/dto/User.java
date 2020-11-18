package ru.gorinych3.inetshop.dto;

import java.math.BigDecimal;

public class User {

    private BigDecimal userId;

    private String login;

    private String password;

    private BigDecimal clientId;

    public BigDecimal getUserId() {
        return userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getClientId() {
        return clientId;
    }

    public void setClientId(BigDecimal clientId) {
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", clientId=" + clientId +
                '}';
    }
}
