package ru.gorinych3.inetshop.dto;

import java.math.BigInteger;

public class Admin {

    private BigInteger adminId;

    private String adminName;

    private String login;

    private String password;

    public Admin() {
    }

    public Admin(String adminName, String login, String password) {
        this.adminName = adminName;
        this.login = login;
        this.password = password;
    }

    public BigInteger getAdminId() {
        return adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
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

    @Override
    public String toString() {
        return "Admin{" +
                "adminId=" + adminId +
                ", adminName='" + adminName + '\'' +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
