package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Jdbi;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.Order;
import vn.edu.hcmuaf.fit.ttltw.model.ShippingZoneFees;
import vn.edu.hcmuaf.fit.ttltw.service.SuperAIService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShippingDao {
    private final Jdbi jdbi; // Kết nối database

    public ShippingDao() {
        this.jdbi = DBConnect.getJdbi();
    }
//    private final SuperAIService superAIService = new SuperAIService();
//       public List<ShippingZoneFees> getListFeesByProvince(String provinceName) {
//        if (provinceName == null || provinceName.trim().isEmpty()) return new ArrayList<>();
//        String cleanProvince = provinceName.trim();
//        String sql = "SELECT f.* FROM shipping_zone_fees f JOIN shipping_zones z ON f.zone_id = z.id WHERE LOWER(z.provinces) LIKE LOWER(:province) AND f.is_active = 1 AND z.is_active = 1";
//
//        return jdbi.withHandle(handle -> handle.createQuery(sql)
//                .bind("province", "%" + cleanProvince + "%")
//                .mapToBean(ShippingZoneFees.class)
//                .list());
//    }


    // tìm đơn hàng theo ID
    public Optional<Order> findOrderById(int id) {
        String sql = "SELECT id, tracking_number, partner_name, status FROM orders WHERE id = :id LIMIT 1";
        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind("id", id)
                .mapToBean(Order.class)
                .findOne());
    }

// lưu mã vận chuyển khi gọi API thành công
    public boolean updateTrackingInfo(int orderId, String tracking, String partner) {
        String sql = "UPDATE orders SET tracking_number = :tracking, partner_name = :partner WHERE id = :id";
        return jdbi.withHandle(handle -> handle.createUpdate(sql)
                .bind("tracking", tracking)
                .bind("partner", partner)
                .bind("id", orderId)
                .execute() > 0);
    }
    // trả về trang thái mới nhất của đơn hàng sau khi cập nhật trạng thái vận chuyển
    public boolean updateOnlyShippingStatus(int orderId, int newStatus) {
        String sql = "UPDATE orders SET  status = ?, updated_at = NOW() WHERE id = ?";
        return jdbi.withHandle(handle -> handle.createUpdate(sql)
                .bind(0, newStatus)
                .bind(1, orderId)
                .execute()) > 0;
    }
    // tìm ID đơn hàng dựa trên mã vận đơn   trả về từ đơn vị vận chuyển
public int getOrderIdByTracking(String tracking) {
    String sql = "SELECT id FROM orders WHERE tracking_number = :tracking";
    return jdbi.withHandle(handle -> handle.createQuery(sql)
            .bind("tracking", tracking)
            .mapTo(Integer.class)
            .findOne()
            .orElse(0)); // không tìm thấy đơn hàng khớp với mã
}
    public String getTrackingByOrderId(int orderId) {
        String sql = "SELECT tracking_number FROM orders WHERE id = :id";

        return jdbi.withHandle(handle ->
                handle.createQuery(sql)
                        .bind("id", orderId)
                        .mapTo(String.class)
                        .findOne()
                        .orElse(null)
        );
    }
}
