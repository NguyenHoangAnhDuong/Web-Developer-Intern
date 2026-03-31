package vn.edu.hcmuaf.fit.ttltw.service;

import java.util.List;
import java.util.Map;

public interface CartService {
     boolean addToCart(int userId, int variantId, int quantity);
     boolean updateCartItem(int userId,int variantId, int delta);
     boolean removeCartItem(int userId, int variantId);
     void clearCart(int userId);
     double calculateCartTotal(int userId);
     List<Map<String, Object>> getCartForDisplay(int userId);
     boolean changeVariant(int userId, int oldVcId, int newVcId);
    List<Map<String, Object>> getColorsByVariant(int variantId);
    Map<String, Object> getVariantInfo(int vcId);
}
