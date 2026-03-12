package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Jdbi;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.Address;
import vn.edu.hcmuaf.fit.ttltw.model.Order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrderDao {
    private final Jdbi jdbi;

    public OrderDao() {
        this.jdbi = DBConnect.getJdbi();
    }

    public List<Order> getOrdersByUserId(int userId) {
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";
        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind(0, userId)
                .mapToBean(Order.class)
                .list());
    }

    public List<Order> getOrdersByUserIdAndStatus(int userId, int status) {
        String sql = "SELECT * FROM orders WHERE user_id = ? AND status = ? ORDER BY created_at DESC";
        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind(0, userId)
                .bind(1, status)
                .mapToBean(Order.class)
                .list());
    }

    public Optional<Order> getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind(0, orderId)
                .mapToBean(Order.class)
                .findFirst());
    }

    public int createOrder(Order order) {
        String sql = "INSERT INTO orders (status, voucher_id, payment_type_id, fee_shipping, " +
                "total_amount, discount_amount, user_id, address_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        return jdbi.withHandle(handle -> handle.createUpdate(sql)
                .bind(0, order.getStatus())
                .bind(1, order.getVoucherId())
                .bind(2, order.getPaymentTypeId())
                .bind(3, order.getFeeShipping())
                .bind(4, order.getTotalAmount())
                .bind(5, order.getDiscountAmount())
                .bind(6, order.getUserId())
                .bind(7, order.getAddressId())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Integer.class)
                .one());
    }

    public boolean updateOrderStatus(int orderId, int status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        return jdbi.withHandle(handle -> handle.createUpdate(sql)
                .bind(0, status)
                .bind(1, orderId)
                .execute()) > 0;
    }

    public boolean cancelOrder(int orderId, int userId) {
        String sql = "UPDATE orders SET status = 4 WHERE id = ? AND user_id = ? AND status = 1";
        return jdbi.withHandle(handle -> handle.createUpdate(sql)
                .bind(0, orderId)
                .bind(1, userId)
                .execute()) > 0;
    }

    // Lấy tất cả order
    public List<Map<String, Object>> findAll() {
        String sql = "SELECT o.*, a.name AS customer_name, a.phone_number AS customer_phone " +
                "FROM orders o " +
                "JOIN addresses a ON o.address_id = a.id " +
                "ORDER BY o.created_at DESC";

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .map((rs, ctx) -> {
                    Map<String, Object> map = new HashMap<>();
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setStatus(rs.getInt("status"));
                    order.setPaymentTypeId(rs.getInt("payment_type_id"));
                    order.setTotalAmount(rs.getDouble("total_amount"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setAddressId(rs.getInt("address_id"));
                    order.setCreatedAt(rs.getTimestamp("created_at"));
                    order.setUpdatedAt(rs.getTimestamp("updated_at"));

                    map.put("order", order);
                    map.put("customerName", rs.getString("customer_name"));
                    map.put("customerPhone", rs.getString("customer_phone"));
                    return map;
                }).list());
    }

    // Tìm kiếm theo mã đơn hàng và tên khách
    public List<Map<String, Object>> searchOrders(String keyword, Integer status) {
        StringBuilder sql = new StringBuilder(
                "SELECT o.*, a.name AS customer_name, a.phone_number AS customer_phone " +
                        "FROM orders o " +
                        "JOIN addresses a ON o.address_id = a.id " +
                        "WHERE 1=1 ");

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (o.id LIKE :kw OR a.name LIKE :kw) ");
        }
        if (status != null) {
            sql.append("AND o.status = :status ");
        }

        return jdbi.withHandle(handle -> {
            var query = handle.createQuery(sql.toString());
            if (keyword != null && !keyword.isEmpty()) {
                query.bind("kw", "%" + keyword + "%");
            }
            if (status != null) {
                query.bind("status", status);
            }

            return query.map((rs, ctx) -> {
                Map<String, Object> map = new HashMap<>();
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setStatus(rs.getInt("status"));
                order.setPaymentTypeId(rs.getInt("payment_type_id"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setUserId(rs.getInt("user_id"));
                order.setAddressId(rs.getInt("address_id"));
                order.setCreatedAt(rs.getTimestamp("created_at"));
                order.setUpdatedAt(rs.getTimestamp("updated_at"));

                map.put("order", order);
                map.put("customerName", rs.getString("customer_name"));
                map.put("customerPhone", rs.getString("customer_phone"));
                return map;
            }).list();
        });
    }

    // Cập nhật trạng thái đơn hàng
    public boolean updateStatus(int orderId, int status) {
        String sql = """
                UPDATE orders
                SET status = :status,
                    updated_at = NOW()
                WHERE id = :id
                """;

        return jdbi.withHandle(handle -> handle.createUpdate(sql)
                .bind("status", status)
                .bind("id", orderId)
                .execute()) > 0;
    }

    // Lấy địa chỉ mặc định của user
    public List<Address> getDefaultAddressByUserId(int userId) {
        return jdbi.withHandle(
                handle -> handle.createQuery("SELECT * FROM addresses WHERE user_id = :userId AND status = 1")
                        .bind("userId", userId)
                        .mapToBean(Address.class)
                        .list() // Trả về danh sách, nếu không có sẽ là list rỗng
        );
    }

    // Lấy thông tin sản phẩm trong giỏ hàng
    public Map<String, Object> getProductForCart(int variantColorId) {
        String sql = """
                    SELECT
                        vc.id AS vc_id,
                        p.name AS product_name,
                        v.name AS variant_name,
                        col.name AS color_name,
                        vc.price AS unit_price,
                        p.img AS main_img,
                        p.discount_percentage
                    FROM variant_colors vc
                    JOIN product_variants v ON vc.variant_id = v.id
                    JOIN products p ON v.product_id = p.id
                    JOIN colors col ON vc.color_id = col.id
                    WHERE vc.id = :vcId AND vc.status = 1
                """;

        return jdbi.withHandle(h -> h.createQuery(sql)
                .bind("vcId", variantColorId)
                .mapToMap()
                .findFirst()
                .orElse(null));
    }

    // Chèn đơn hàng cùng chi tiết đơn hàng trong transaction
    public int insertOrderWithDetails(Order order, Map<Integer, Integer> cart) {
        return jdbi.withHandle(handle -> handle.inTransaction(h -> {
            String sqlOrder = """
                        INSERT INTO orders (user_id, address_id, payment_type_id, voucher_id,
                                           status, fee_shipping, discount_amount, total_amount, created_at)
                        VALUES (:userId, :addressId, :paymentTypeId, :voucherId,
                               :status, :feeShipping, :discountAmount, :totalAmount, NOW())
                    """;
            int orderId = h.createUpdate(sqlOrder)
                    .bindBean(order)
                    .executeAndReturnGeneratedKeys("id")
                    .mapTo(Integer.class)
                    .one();

            String sqlDetail = """
                        INSERT INTO order_details (order_id, variant_id, quantity, price, total_money)
                        VALUES (:orderId, :vcId, :quantity, :price, :total_money)
                    """;
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                int vcId = entry.getKey();
                int qty = entry.getValue();
                // Lấy giá hiện tại từ DB để lưu vào hóa đơn (tránh việc sau này sản phẩm đổi
                // giá)
                Map<String, Object> product = getProductForCart(vcId);
                if (product == null) {
                    throw new RuntimeException("Sản phẩm với ID " + vcId + " không tồn tại hoặc đã bị xóa");
                }
                double unitPrice = ((BigDecimal) product.get("unit_price")).doubleValue();
                // Tính giá sau discount (nếu có)
                Object discountObj = product.get("discount_percentage");
                double discountPercent = 0;
                if (discountObj != null) {
                    if (discountObj instanceof BigDecimal) {
                        discountPercent = ((BigDecimal) discountObj).doubleValue();
                    } else if (discountObj instanceof Integer) {
                        discountPercent = ((Integer) discountObj).doubleValue();
                    } else if (discountObj instanceof Double) {
                        discountPercent = (Double) discountObj;
                    }
                }
                // Giá mới = giá gốc * (1 - discount%)
                double price = unitPrice * (100 - discountPercent) / 100;
                h.createUpdate(sqlDetail)
                        .bind("orderId", orderId)
                        .bind("vcId", vcId)
                        .bind("quantity", qty)
                        .bind("price", price)
                        .bind("total_money", price * qty)
                        .execute();
                h.createUpdate("UPDATE variant_colors SET quantity = quantity - :qty WHERE id = :vcId")
                        .bind("qty", qty)
                        .bind("vcId", vcId)
                        .execute();
            }

            return orderId;
        }));
    }
}