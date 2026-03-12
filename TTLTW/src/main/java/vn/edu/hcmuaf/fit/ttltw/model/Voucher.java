package vn.edu.hcmuaf.fit.ttltw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Voucher implements Serializable{
    private int id;
    private String voucherCode;
    private int discountAmount;
    private int type;
    private int status;
    private int minOrderValue;
    private int maxReduce;
    private int quantity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Voucher() {
    }

    public int getId() {
        return id;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public int getDiscountAmount() {
        return discountAmount;
    }

    public int getType() {
        return type;
    }

    public int getStatus() {
        return status;
    }

    public int getMinOrderValue() {
        return minOrderValue;
    }

    public int getMaxReduce() {
        return maxReduce;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public void setDiscountAmount(int discountAmount) {
        this.discountAmount = discountAmount;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMinOrderValue(int minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public void setMaxReduce(int maxReduce) {
        this.maxReduce = maxReduce;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "VoucherAdmin{" +
                "id=" + id +
                ", voucherCode='" + voucherCode + '\'' +
                ", discountAmount=" + discountAmount +
                ", type=" + type +
                ", status=" + status +
                ", minOrderValue=" + minOrderValue +
                ", maxReduce=" + maxReduce +
                ", quantity=" + quantity +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}