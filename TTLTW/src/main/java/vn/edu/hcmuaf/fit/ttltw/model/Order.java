package vn.edu.hcmuaf.fit.ttltw.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Order implements Serializable{
    private int id;
    private int status;
    private Integer voucherId;
    private int paymentTypeId;
    private double feeShipping;
    private double totalAmount;
    private double discountAmount;
    private int userId;
    private int addressId;
    private String note;
    private String trackingNumber;
    private String partnerName;
    private Timestamp createdAt;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    private Timestamp updatedAt;

    public Order() {
    }

    public Order(int id, int status, Integer voucherId, int paymentTypeId, double feeShipping, double totalAmount, double discountAmount, int userId, int addressId, String note, String trackingNumber, String partnerName, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.status = status;
        this.voucherId = voucherId;
        this.paymentTypeId = paymentTypeId;
        this.feeShipping = feeShipping;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.userId = userId;
        this.addressId = addressId;
        this.note = note;
        this.trackingNumber = trackingNumber;
        this.partnerName = partnerName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Integer getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Integer voucherId) {
        this.voucherId = voucherId;
    }

    public int getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(int paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public double getFeeShipping() {
        return feeShipping;
    }

    public void setFeeShipping(double feeShipping) {
        this.feeShipping = feeShipping;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
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
        return "Order{" +
                "id=" + id +
                ", status=" + status +
                ", voucherId=" + voucherId +
                ", paymentTypeId=" + paymentTypeId +
                ", feeShipping=" + feeShipping +
                ", totalAmount=" + totalAmount +
                ", discountAmount=" + discountAmount +
                ", userId=" + userId +
                ", addressId=" + addressId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
