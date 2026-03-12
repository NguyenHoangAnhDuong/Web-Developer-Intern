package vn.edu.hcmuaf.fit.ttltw.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Product implements Serializable {
    private int id;
    private String name;
    private String description;
    private int discountPercentage;
    private int totalSold;
    private int warrantyPeriod;
    private int status;

    private String mainImage; // Ảnh đại diện duy nhất

    private Category category;
    private Brand brand;

    private LocalDateTime releaseDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TechSpecs> techSpecs;
    private List<ProductVariant> variants;

    public Product() {
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

    public int getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(int totalSold) {
        this.totalSold = totalSold;
    }

    public int getWarrantyPeriod() {
        return warrantyPeriod;
    }

    public void setWarrantyPeriod(int warrantyPeriod) {
        this.warrantyPeriod = warrantyPeriod;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
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

    public List<ProductVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants;
    }

    public List<TechSpecs> getTechSpecs() {
        return techSpecs;
    }

    public void setTechSpecs(List<TechSpecs> techSpecs) {
        this.techSpecs = techSpecs;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id && discountPercentage == product.discountPercentage && totalSold == product.totalSold && warrantyPeriod == product.warrantyPeriod && status == product.status && Objects.equals(name, product.name) && Objects.equals(description, product.description) && Objects.equals(mainImage, product.mainImage) && Objects.equals(category, product.category) && Objects.equals(brand, product.brand) && Objects.equals(releaseDate, product.releaseDate) && Objects.equals(createdAt, product.createdAt) && Objects.equals(updatedAt, product.updatedAt) && Objects.equals(techSpecs, product.techSpecs) && Objects.equals(variants, product.variants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, discountPercentage, totalSold, warrantyPeriod, status, mainImage, category, brand, releaseDate, createdAt, updatedAt, techSpecs, variants);
    }
}
