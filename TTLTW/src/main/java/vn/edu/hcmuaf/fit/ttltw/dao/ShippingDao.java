package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Jdbi;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.ShippingZoneFees;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShippingDao {
    private final Jdbi jdbi; // Kết nối database

    public ShippingDao() {
        this.jdbi = DBConnect.getJdbi();
    }

  public List<ShippingZoneFees> getListFeesByProvince(String provinceName) {
    if (provinceName == null || provinceName.trim().isEmpty()) return new ArrayList<>();
    String cleanProvince = provinceName.trim();
    String sql = "SELECT f.* FROM shipping_zone_fees f JOIN shipping_zones z ON f.zone_id = z.id WHERE LOWER(z.provinces) LIKE LOWER(:province) AND f.is_active = 1 AND z.is_active = 1";

    return jdbi.withHandle(handle -> handle.createQuery(sql)
            .bind("province", "%" + cleanProvince + "%")
            .mapToBean(ShippingZoneFees.class)
            .list());
}
    // lưu mã vẫn chuyển khi đơn sau khi gọi API thành công
    public  boolean updateShippingInfo(int orderId, String tracking, String partner) {
        String sql = "UPDATE orders SET tracking_number = :tracking, shipping_partner = :partner, status = :status  WHERE id = :id";
        return jdbi.withHandle(handle -> handle.createUpdate(sql)
                .bind("tracking", tracking)
                .bind("partner", partner)
                .bind("status", "Đang xử lý") // Trạng thái mặc định khi vừa đẩy đơn sang SuperAI
                .bind("id", orderId)
                .execute() > 0);
    }
    // trả về trang thái mới nhất của đơn hàng sau khi cập nhật trạng thái vận chuyển
    public boolean updateOnlyShippingStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET  status = ?, updated_at = NOW() WHERE id = ?";
        return jdbi.withHandle(handle -> handle.createUpdate(sql)
                .bind(0, newStatus)
                .bind(1, orderId)
                .execute()) > 0;
    }
    // Tìm ID đơn hàng dựa trên mã vận đơn   trả về từ đơn vị vận chuyển
public int getOrderIdByTracking(String tracking) {
    String sql = "SELECT id FROM orders WHERE tracking_number = :tracking";
    return jdbi.withHandle(handle -> handle.createQuery(sql)
            .bind("tracking", tracking)
            .mapTo(Integer.class)
            .findOne()
            .orElse(0)); // Trả về 0 nếu không tìm thấy đơn hàng nào khớp với mã này
}
}
