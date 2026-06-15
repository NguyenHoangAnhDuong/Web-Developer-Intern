package vn.edu.hcmuaf.fit.ttltw.service;

import java.util.List;
import java.util.Map;

import vn.edu.hcmuaf.fit.ttltw.dao.ShippingDao;
import vn.edu.hcmuaf.fit.ttltw.model.ShippingZoneFees;

public class ShippingService {
    private final ShippingDao shippingDao = new ShippingDao();
    private final SuperAIService superAI = new SuperAIService();
    // cập nhập thông tin vẫn chuyển của đơn hàng
    public boolean updateTrackingInfo(int orderId, String tracking, String partner) {
        return shippingDao.updateTrackingInfo(orderId, tracking, partner);
    }
// Xử lý nghiệp vụ vận chuyển
    public String createShipment(int orderId, String name, String phone, String address, double amount, double orderValue) {

        System.out.println("Creating shipment for order " + orderId);

        String tracking = superAI.createRealOrder(
                orderId, name, phone, address, amount, orderValue
        );

        if (tracking != null) {
            boolean updated = shippingDao.updateTrackingInfo(orderId, tracking, "SuperAI");

            if (updated) {
                System.out.println(" tracking saved: " + tracking);
            } else {
                System.err.println("failed to save tracking");
            }
        }

        return tracking;
    }

    public List<Map<String, Object>> getShippingCarriers() {
        return superAI.getShippingCarriers();
    }

    public List<ShippingZoneFees> getShippingServices(String fullAddress, long weight, long value) {
        return superAI.getShippingFeeOptions(fullAddress, weight, value);
    }
}
