package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Jdbi;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CartDAO {
    private final Jdbi jdbi = DBConnect.getJdbi();

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
                        subtotal = (quantity + :qty) * :price
                """)
                .bind("cartId", cartId).bind("vId", variantId)
                .bind("qty", qty).bind("price", price)
                .execute());
    }

    // Lấy chi tiết để hiển thị
    public List<Map<String, Object>> getCartDetails(int cartId) {
        return jdbi.withHandle(handle -> {

            List<Map<String, Object>> items = handle
                    .createQuery("""
                    SELECT ci.*,
                           ci.variant_id as vc_id,
                           p.id as product_id,
                           p.name as product_name, 
                           p.img as product_img, 
                           pv.name as variant_name, 
                           c.name as color_name,
                           vc.quantity as stock
                    FROM cart_items ci
                    JOIN variant_colors vc ON ci.variant_id = vc.id
                    JOIN product_variants pv ON vc.variant_id = pv.id
                    JOIN products p ON pv.product_id = p.id
                    JOIN colors c ON vc.color_id = c.id
                    WHERE ci.cart_id = :cartId
                """)
                    .bind("cartId", cartId)
                    .mapToMap()
                    .list();



            for (Map<String, Object> item : items) {
                int productId = Integer.parseInt(item.get("product_id").toString());

                List<Map<String, Object>> variants = handle
                        .createQuery("""
                select distinct pv.id as variant_id, pv.name as variant_name
                from product_variants pv
                where pv.product_id = :pId
            """)
                        .bind("pId", productId)
                        .mapToMap()
                        .list();

                int currentVariantId = handle
                        .createQuery("""
                SELECT variant_id FROM variant_colors WHERE id = :vcId
            """)
                        .bind("vcId", item.get("vc_id"))
                        .mapTo(Integer.class)
                        .one();

                List<Map<String, Object>> colors = handle
                        .createQuery("""
                SELECT vc.id, c.name as color_name
                FROM variant_colors vc
                JOIN colors c ON vc.color_id = c.id
                WHERE vc.variant_id = :variantId
            """)
                        .bind("variantId", currentVariantId)
                        .mapToMap()
                        .list();

                item.put("variants", variants);
                item.put("colors", colors);
                item.put("variant_id", currentVariantId);
            }

            return items;
        });
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

    // Lấy danh sách màu theo variant
    public List<Map<String, Object>> getColorsByVariant(int variantId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT vc.id, c.name AS color_name
                FROM variant_colors vc
                JOIN colors c ON vc.color_id = c.id
                WHERE vc.variant_id = :variantId
            """)
                        .bind("variantId", variantId)
                        .mapToMap()
                        .list()
        );
    }

    // Lấy giá + tồn kho
    public Map<String, Object> getVariantInfo(int vcId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                SELECT price, quantity
                FROM variant_colors
                WHERE id = :id
            """)
                        .bind("id", vcId)
                        .mapToMap()
                        .one()
        );
    }
}
