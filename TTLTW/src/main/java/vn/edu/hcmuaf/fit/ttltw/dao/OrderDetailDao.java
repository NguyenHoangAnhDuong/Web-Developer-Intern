package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Jdbi;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.OrderDetail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDetailDao {
    private final Jdbi jdbi;

    public OrderDetailDao() {
        this.jdbi = DBConnect.getJdbi();
    }

    public List<OrderDetail> getOrderDetailsByOrderId(int orderId) {
        String sql = "SELECT * FROM order_details WHERE order_id = ?";
        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind(0, orderId)
                .mapToBean(OrderDetail.class)
                .list());
    }

    public List<OrderDetail> getOrderDetailsWithProduct(int orderId) {
        return getOrderDetailsByOrderId(orderId);
    }

    public Map<String, String> getProductInfoByVariantId(int variantId) {
        String sql = "SELECT p.id AS product_id, p.name as product_name, pv.name as variant_name, " +
                "c.name as color_name, c.color_code, " +
                "COALESCE(i.img_path, p.img) as image_path " +
                "FROM variant_colors vc " +
                "JOIN product_variants pv ON vc.variant_id = pv.id " +
                "JOIN products p ON pv.product_id = p.id " +
                "JOIN colors c ON vc.color_id = c.id " +
                "LEFT JOIN images i ON i.variant_color_id = vc.id " +
                "WHERE vc.id = ? " +
                "LIMIT 1";

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind(0, variantId)
                .map((rs, ctx) -> {
                    Map<String, String> info = new HashMap<>();
                    info.put("productId", String.valueOf(rs.getInt("product_id")));
                    info.put("productName", rs.getString("product_name"));
                    info.put("variantName", rs.getString("variant_name"));
                    info.put("colorName", rs.getString("color_name"));
                    info.put("colorCode", rs.getString("color_code"));
                    info.put("imagePath", rs.getString("image_path"));
                    return info;
                })
                .findFirst()
                .orElse(new HashMap<>()));
    }

    public boolean hasUserPurchasedProduct(int userId, int productId) {
        String sql = "SELECT 1 " +
                "FROM orders o " +
                "JOIN order_details od ON o.id = od.order_id " +
                "JOIN variant_colors vc ON od.variant_id = vc.id " +
                "JOIN product_variants pv ON vc.variant_id = pv.id " +
                "WHERE o.user_id = ? " +
                "AND pv.product_id = ? " +
                "AND o.status <> 4 " +
                "LIMIT 1";

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind(0, userId)
                .bind(1, productId)
                .mapTo(Integer.class)
                .findOne()
                .isPresent());
    }

    public boolean addOrderDetail(OrderDetail orderDetail) {
        String sql = "INSERT INTO order_details (variant_id, order_id, price, quantity, " +
                "discount_amount, total_money) VALUES (?, ?, ?, ?, ?, ?)";

        return jdbi.withHandle(handle -> handle.createUpdate(sql)
                .bind(0, orderDetail.getVariantId())
                .bind(1, orderDetail.getOrderId())
                .bind(2, orderDetail.getPrice())
                .bind(3, orderDetail.getQuantity())
                .bind(4, orderDetail.getDiscountAmount())
                .bind(5, orderDetail.getTotalMoney())
                .execute()) > 0;
    }
}