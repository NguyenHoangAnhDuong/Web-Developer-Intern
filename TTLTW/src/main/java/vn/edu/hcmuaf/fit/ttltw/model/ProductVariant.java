package vn.edu.hcmuaf.fit.ttltw.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class ProductVariant implements Serializable{
    private int id;
    private String name;        // 64GB, 128GB, 256GB
    private double basePrice;
    private int status;
    private List<VariantColor> colors;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductVariant() {
    }

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

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<VariantColor> getColors() {
        return colors;
    }

    public void setColors(List<VariantColor> colors) {
        this.colors = colors;
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
