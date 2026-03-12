package vn.edu.hcmuaf.fit.ttltw.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class OrderDetail implements Serializable {
    private int id;
    private int variantId;
    private int orderId;

    private double price;
    private int quantity;
    private double discountAmount;
    private double totalMoney;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructor không tham số
    public OrderDetail() {
    }

    public OrderDetail(int id, int variantId, int orderId, double price, int quantity, double discountAmount, double totalMoney, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.variantId = variantId;
        this.orderId = orderId;
        this.price = price;
        this.quantity = quantity;
        this.discountAmount = discountAmount;
        this.totalMoney = totalMoney;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(double totalMoney) {
        this.totalMoney = totalMoney;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "OrderDetail{" +
                "id=" + id +
                ", variantId=" + variantId +
                ", orderId=" + orderId +
                ", price=" + price +
                ", quantity=" + quantity +
                ", discountAmount=" + discountAmount +
                ", totalMoney=" + totalMoney +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
