package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.ShippingDao;

public class ShippingService {
    private ShippingDao shippingDao = new ShippingDao();
    private final SuperAIService superAI = new SuperAIService();
    // cập nhập thông tin vẫn chuyển của đơn hàng
    public boolean updateTrackingInfo(int orderId, String tracking, String partner) {
        return shippingDao.updateTrackingInfo(orderId, tracking, partner);
    }
// Xử lý nghiệp vụ vận chuyển
    public String createShipment(int orderId, String name, String phone, String address, double amount) {

        System.out.println("Creating shipment for order " + orderId);

        String tracking = superAI.createRealOrder(
                orderId, name, phone, address, amount
        );

        if (tracking != null) {
            boolean updated = shippingDao.updateTrackingInfo(orderId, tracking, "SuperShip");

            if (updated) {
                System.out.println(" tracking saved: " + tracking);
            } else {
                System.err.println("failed to save tracking");
            }
        }

        return tracking;
    }
}
