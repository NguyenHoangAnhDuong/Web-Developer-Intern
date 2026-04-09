package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.ShippingDao;

public class ShippingService {
    private ShippingDao shippingDao = new ShippingDao();
    // cập nhập thông tin vẫn chuyển của đơn hàng
    public boolean updateShippingInfo(int orderId, String tracking, String partner) {
        return shippingDao.updateShippingInfo(orderId, tracking, partner);
    }
}
