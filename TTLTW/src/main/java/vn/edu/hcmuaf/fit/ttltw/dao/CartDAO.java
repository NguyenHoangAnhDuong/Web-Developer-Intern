package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Jdbi;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CartDAO {
    private final Jdbi jdbi = DBConnect.getJdbi();;

    //   Tìm giỏ hàng của user
    public Optional<Integer> getActiveCartId(int userId) {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT id FROM carts WHERE user_id = :userId AND status = 'active' LIMIT 1")
                .bind("userId", userId)
                .mapTo(Integer.class)
                .findOne());
    }

    //  Tạo giỏ hàng mới
    public int createCart(int userId) {
        return jdbi.withHandle(handle -> handle
                .createUpdate("INSERT INTO carts (user_id, status) VALUES (:userId, 'active')")
                .bind("userId", userId)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Integer.class)
                .one());
    }

    //  Kiểm tra tồn kho
    public int getStock(int variantId) {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT quantity FROM variant_colors WHERE id = :vId")
                .bind("vId", variantId)
                .mapTo(Integer.class)
                .findOne().orElse(0));
    }

    // Lấy giá hiện tại
    public double getPrice(int variantId) {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT price FROM variant_colors WHERE id = :vId")
                .bind("vId", variantId)
                .mapTo(Double.class)
                .one());
    }

    // Cập nhật item
    public void upsertItem(int cartId, int variantId, int qty, double price) {
        jdbi.useHandle(handle -> handle
                .createUpdate("""
                    INSERT INTO cart_items (cart_id, variant_id, quantity, unit_price, subtotal)
                    VALUES (:cartId, :vId, :qty, :price, :qty * :price)
                    ON DUPLICATE KEY UPDATE 
                        quantity = quantity + :qty,
                        subtotal = (quantity + :qty) * unit_price
                """)
                .bind("cartId", cartId).bind("vId", variantId)
                .bind("qty", qty).bind("price", price)
                .execute());
    }

    // Lấy chi tiết để hiển thị
    public List<Map<String, Object>> getCartDetails(int cartId) {
        return jdbi.withHandle(handle -> handle
                .createQuery("""
                    SELECT ci.*, p.name as product_name, p.img as product_img, 
                           pv.name as variant_name, c.name as color_name
                    FROM cart_items ci
                    JOIN variant_colors vc ON ci.variant_id = vc.id
                    JOIN product_variants pv ON vc.variant_id = pv.id
                    JOIN products p ON pv.product_id = p.id
                    JOIN colors c ON vc.color_id = c.id
                    WHERE ci.cart_id = :cartId
                """)
                .bind("cartId", cartId)
                .mapToMap()
                .list());
    }
    // kiểm tra xem item đã có trong giỏ chưa
    public int getItemQtyInCart(int cartId, int vId) {
        return jdbi.withHandle(handle -> handle
                .createQuery("SELECT quantity FROM cart_items WHERE cart_id = :cartId AND variant_id = :vId")
                .bind("cartId", cartId)
                .bind("vId", vId)
                .mapTo(Integer.class)
                .findOne()
                .orElse(0));
    }
    // xóa sạch khỏi giỏ
    public void deleteAllItems(int cartId) {
        jdbi.useHandle(handle -> handle
                .createUpdate("DELETE FROM cart_items WHERE cart_id = :cartId")
                .bind("cartId", cartId)
                .execute());
    }
    // tăng giảm số lượng +/- trong giỏ
    public void updateQty(int cartId, int variantId, int delta) {
        jdbi.useHandle(handle -> handle
                .createUpdate("""
                    UPDATE cart_items 
                    SET quantity = quantity + :delta, 
                        subtotal = (quantity + :delta) * unit_price,
                        updated_at = NOW()
                    WHERE cart_id = :cartId AND variant_id = :vId
                """)
                .bind("cartId", cartId)
                .bind("vId", variantId)
                .bind("delta", delta)
                .execute());
    }
    // xóa 1 item khỏi giỏ
    public void deleteItem(int cartId, int variantId) {
        jdbi.useHandle(handle -> handle
                .createUpdate("DELETE FROM cart_items WHERE cart_id = :cartId AND variant_id = :vId")
                .bind("cartId", cartId)
                .bind("vId", variantId)
                .execute());
    }

}
