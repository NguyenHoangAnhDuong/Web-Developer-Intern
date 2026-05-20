package vn.edu.hcmuaf.fit.ttltw.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Feedback implements Serializable {

    private int id;
    private int productId;
    private String productName;
    private int userId;
    private int orderId;
    private int rating;
    private String comment;
    private int status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;



    public Feedback() {
    }

    // ===== Constructor đầy đủ =====
    public Feedback(int id, int productId, int userId, int orderId,int rating,
                    String comment, int status,
                    LocalDateTime createdAt, LocalDateTime updatedAt, String username) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.username = username;
    }

    // ===== Getter & Setter =====
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    @SuppressWarnings("unused")
    public String getStatusLabel() {
        return switch (status) {
            case 1  -> "show";
            case 0  -> "hide";
            case 2  -> "pending";
            default -> "unknown";
        };
    }
    @SuppressWarnings("unused")
    public String getUserInitial() {
        if (username != null && !username.isEmpty()) {
            return String.valueOf(username.charAt(0)).toUpperCase();
        }
        return "U";
    }
    @SuppressWarnings("unused")
    public String getCreatedAtFormatted() {
        if (createdAt == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return createdAt.format(formatter);
    }
}
