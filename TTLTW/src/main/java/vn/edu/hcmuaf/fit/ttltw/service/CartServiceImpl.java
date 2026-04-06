package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.CartDAO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CartServiceImpl implements CartService {
    private final CartDAO cartDao = new CartDAO();
// thêm sản phâm vào giỏ hàng, nếu đã có thì cộng dồn số lượng, nếu không có thì tạo mới
    @Override
    public boolean addToCart(int userId, int variantId, int quantity) {
        int stock = cartDao.getStock(variantId);
        if (stock < quantity) {
            return false;
        }
        Optional<Integer> cartIdOptional = cartDao.getActiveCartId(userId);
        int cartId;

        if (cartIdOptional.isPresent()) {
            cartId = cartIdOptional.get();
        } else {
            cartId = cartDao.createCart(userId);
        }
        double price = cartDao.getPrice(variantId);
        cartDao.upsertItem(cartId, variantId, quantity, price);

        return true;
    }
// delta > 0: tăng số lượng, delta < 0: giảm số lượng
    @Override
    public boolean updateCartItem(int userId,int variantId, int delta) {
        Optional<Integer> cartIdOptional = cartDao.getActiveCartId(userId);

        if (cartIdOptional.isPresent()) {
            int cartId = cartIdOptional.get();
            int stock = cartDao.getStock(variantId);
            int currentInCart = cartDao.getItemQtyInCart(cartId, variantId);

            if (delta > 0 && (currentInCart + delta > stock)) {
                return false;
            }
            cartDao.updateQty(cartId, variantId, delta);

            if (currentInCart + delta <= 0) {
                cartDao.deleteItem(cartId, variantId);
            }
            return true;
        }
        return false;
    }
// xóa sản phẩm khỏi giỏ hàng
    @Override
    public boolean removeCartItem(int userId, int variantId) {
        Optional<Integer> cartIdOptional = cartDao.getActiveCartId(userId);
        if (cartIdOptional.isPresent()) {
            cartDao.deleteItem(cartIdOptional.get(), variantId);
            return true;
        }
        return false;
    }
// xóa tất cả sản phẩm khỏi giỏ hàng
    @Override
    public void clearCart(int userId) {
        Optional<Integer> cartIdOptional = cartDao.getActiveCartId(userId);

        if (cartIdOptional.isPresent()) {
            cartDao.deleteAllItems(cartIdOptional.get());
        }
    }
    // tính tổng tiền của giỏ hàng
    @Override
    public double calculateCartTotal(int userId) {
        Optional<Integer> cartIdOptional = cartDao.getActiveCartId(userId);
        double total = 0;

        if (cartIdOptional.isPresent()) {
            List<Map<String, Object>> items = cartDao.getCartDetails(cartIdOptional.get());
            for (Map<String, Object> item : items) {
                double subtotal = Double.parseDouble(item.get("subtotal").toString());
                total += subtotal;
            }
        }
        return total;
    }
    @Override
    //   lấy danh sách hiển thị ra JSP
    public List<Map<String, Object>> getCartForDisplay(int userId) {
        Optional<Integer> cartIdOptional = cartDao.getActiveCartId(userId);
        if (cartIdOptional.isPresent()) {
            return cartDao.getCartDetails(cartIdOptional.get());
        }
        return java.util.Collections.emptyList();
    }
    // đổi biến thể của một item trong giỏ hàng
    @Override
    public boolean changeVariant(int userId, int oldVcId, int newVcId) {
        Optional<Integer> cartIdOptional = cartDao.getActiveCartId(userId);

        if (cartIdOptional.isEmpty()) return false;

        int cartId = cartIdOptional.get();

        int stock = cartDao.getStock(newVcId);
        int qty = cartDao.getItemQtyInCart(cartId, oldVcId);

        if (qty > stock) return false;

        double price = cartDao.getPrice(newVcId);

        cartDao.deleteItem(cartId, oldVcId);
        cartDao.upsertItem(cartId, newVcId, qty, price);

        return true;
    }
// lấy danh sách màu sắc
    @Override
    public List<Map<String, Object>> getColorsByVariant(int variantId) {
        return cartDao.getColorsByVariant(variantId);
    }
// lấy thông tin của biến thể
    @Override
    public Map<String, Object> getVariantInfo(int vcId) {
        return cartDao.getVariantInfo(vcId);
    }

    @Override
    public int getTotalQuantity(int userId) {
        Optional<Integer> cartIdOptional = cartDao.getActiveCartId(userId);
        if (cartIdOptional.isPresent()) {
            List<Map<String, Object>> items = cartDao.getCartDetails(cartIdOptional.get());
            int total = 0;
            for (Map<String, Object> item : items) {
                total += Integer.parseInt(item.get("quantity").toString());
            }
            return total;
        }
        return 0;
    }
}
