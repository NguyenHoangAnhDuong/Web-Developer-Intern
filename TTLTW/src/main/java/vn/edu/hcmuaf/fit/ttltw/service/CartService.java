package vn.edu.hcmuaf.fit.ttltw.service;

public interface CartService {
     boolean addToCart(int userId, int variantId, int quantity);
     boolean updateCartItem(int userId,int variantId, int delta);
     boolean removeCartItem(int userId, int variantId);
     void clearCart(int userId);
     double calculateCartTotal(int userId);
}
