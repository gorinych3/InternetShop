package ru.gorinych3.inetshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Order {

    private BigDecimal orderId;

    private int countItems;

    private BigDecimal sum;

    private String status;

    private LocalDateTime openDate;

    private LocalDateTime executeDate;

    private BigDecimal clientId;

    private List<Item> items;

    public Order() {
    }

    public Order(BigDecimal orderId, int countItems, BigDecimal sum, String status, LocalDateTime openDate,
                 LocalDateTime executeDate, BigDecimal clientId, List<Item> items) {
        this.orderId = orderId;
        this.countItems = countItems;
        this.sum = sum;
        this.status = status;
        this.openDate = openDate;
        this.executeDate = executeDate;
        this.clientId = clientId;
        this.items = items;
    }

    public BigDecimal getOrderId() {
        return orderId;
    }

    public void setOrderId(BigDecimal orderId) {
        this.orderId = orderId;
    }

    public int getCountItems() {
        return countItems;
    }

    public void setCountItems(int countItems) {
        this.countItems = countItems;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDateTime openDate) {
        this.openDate = openDate;
    }

    public LocalDateTime getExecuteDate() {
        return executeDate;
    }

    public void setExecuteDate(LocalDateTime executeDate) {
        this.executeDate = executeDate;
    }

    public BigDecimal getClientId() {
        return clientId;
    }

    public void setClientId(BigDecimal clientId) {
        this.clientId = clientId;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return countItems == order.countItems &&
                Objects.equals(orderId, order.orderId) &&
                Objects.equals(sum, order.sum) &&
                Objects.equals(status, order.status) &&
                Objects.equals(openDate, order.openDate) &&
                Objects.equals(executeDate, order.executeDate) &&
                Objects.equals(clientId, order.clientId) &&
                Objects.equals(items, order.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, countItems, sum, status, openDate, executeDate, clientId, items);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", countItems=" + countItems +
                ", sum=" + sum +
                ", status='" + status + '\'' +
                ", openDate=" + openDate +
                ", executeDate=" + executeDate +
                ", clientId=" + clientId +
                ", items=" + items +
                '}';
    }
}
