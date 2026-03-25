package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.CartDAO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CartServiceImpl implements CartService {
    private final CartDAO cartDao = new CartDAO();

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

    @Override
    public boolean removeCartItem(int userId, int variantId) {
        Optional<Integer> cartIdOptional = cartDao.getActiveCartId(userId);
        if (cartIdOptional.isPresent()) {
            cartDao.deleteItem(cartIdOptional.get(), variantId);
            return true;
        }
        return false;
    }

    @Override
    public void clearCart(int userId) {
        Optional<Integer> cartIdOptional = cartDao.getActiveCartId(userId);

        if (cartIdOptional.isPresent()) {
            cartDao.deleteAllItems(cartIdOptional.get());
        }
    }
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
}
