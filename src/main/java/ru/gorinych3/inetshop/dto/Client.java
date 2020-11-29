package ru.gorinych3.inetshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Client {

    private BigDecimal clientId;

    private String firstName;

    private String lastName;

    private String middleName;

    private String address;

    private String phoneNumber;

    private LocalDateTime registrationDate;

    public Client() {
    }

    public Client(BigDecimal clientId, String firstName, String lastName, String middleName, String address,
                  String phoneNumber, LocalDateTime registrationDate) {
        this.clientId = clientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.registrationDate = registrationDate;
    }

    public Client(String firstName, String lastName, String middleName, String address, String phoneNumber,
                  LocalDateTime registrationDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.registrationDate = registrationDate;
    }

    public BigDecimal getClientId() {
        return clientId;
    }

    public void setClientId(BigDecimal clientId) {
        this.clientId = clientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client)) return false;
        Client client = (Client) o;
        return Objects.equals(clientId, client.clientId) &&
                Objects.equals(firstName, client.firstName) &&
                Objects.equals(lastName, client.lastName) &&
                Objects.equals(middleName, client.middleName) &&
                Objects.equals(address, client.address) &&
                Objects.equals(phoneNumber, client.phoneNumber) &&
                Objects.equals(registrationDate, client.registrationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, firstName, lastName, middleName, address, phoneNumber, registrationDate);
    }

    @Override
    public String toString() {
        return "Client{" +
                "clientId=" + clientId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", registrationDate=" + registrationDate +
                '}';
    }
}
