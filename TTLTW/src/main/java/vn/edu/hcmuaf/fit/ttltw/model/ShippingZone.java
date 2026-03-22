package vn.edu.hcmuaf.fit.ttltw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ShippingZone implements Serializable {
    private int id;
    private String name;
    private String provinces;
    private int isActive;    // tinyint(1) tương ứng 0 hoặc 1
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ShippingZone() {
    }

    public ShippingZone(int id) {
        this.id = id;
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvinces() {
        return provinces;
    }

    public void setProvinces(String provinces) {
        this.provinces = provinces;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}