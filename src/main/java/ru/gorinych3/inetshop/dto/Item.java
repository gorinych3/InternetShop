package ru.gorinych3.inetshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Item {

    private BigDecimal itemId;

    private String itemName;

    private String description;

    private String category;

    private BigDecimal cost;

    private LocalDateTime createDate;

    private LocalDateTime sellDate;

    private String status;

    public Item() {
    }

    public Item(String itemName, String description, String category, BigDecimal cost, LocalDateTime createDate,
                LocalDateTime sellDate, String status) {
        this.itemName = itemName;
        this.description = description;
        this.category = category;
        this.cost = cost;
        this.createDate = createDate;
        this.sellDate = sellDate;
        this.status = status;
    }

    public Item(BigDecimal itemId, String itemName, String description, String category, BigDecimal cost,
                LocalDateTime createDate, LocalDateTime sellDate, String status) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.description = description;
        this.category = category;
        this.cost = cost;
        this.createDate = createDate;
        this.sellDate = sellDate;
        this.status = status;
    }

    public BigDecimal getItemId() {
        return itemId;
    }

    public void setItemId(BigDecimal itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getSellDate() {
        return sellDate;
    }

    public void setSellDate(LocalDateTime sellDate) {
        this.sellDate = sellDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", cost=" + cost +
                ", createDate=" + createDate +
                ", sellDate=" + sellDate +
                ", status='" + status + '\'' +
                '}';
    }
}
