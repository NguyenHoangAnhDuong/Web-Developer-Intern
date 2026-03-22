package vn.edu.hcmuaf.fit.ttltw.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class ShippingZoneFees implements Serializable {
    private int id;
    private int zoneId;
    private String shippingMethod; // enum('standard','express')
    private BigDecimal baseFee;
    private BigDecimal perKgFee;
    private String estimatedDays;
    private int isActive;

    public ShippingZoneFees() {
    }

    public ShippingZoneFees(int id) {
        this.id = id;
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getZoneId() {
        return zoneId;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public BigDecimal getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(BigDecimal baseFee) {
        this.baseFee = baseFee;
    }

    public BigDecimal getPerKgFee() {
        return perKgFee;
    }

    public void setPerKgFee(BigDecimal perKgFee) {
        this.perKgFee = perKgFee;
    }

    public String getEstimatedDays() {
        return estimatedDays;
    }

    public void setEstimatedDays(String estimatedDays) {
        this.estimatedDays = estimatedDays;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }
}