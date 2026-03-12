package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.OrderDetailDao;
import vn.edu.hcmuaf.fit.ttltw.model.OrderDetail;

import java.util.List;
import java.util.Map;

public class OrderDetailService {
    private final OrderDetailDao orderDetailDao;

    public OrderDetailService() {
        this.orderDetailDao = new OrderDetailDao();
    }

    public List<OrderDetail> getOrderDetails(int orderId) {
        return orderDetailDao.getOrderDetailsByOrderId(orderId);
    }

    public List<OrderDetail> getOrderDetailsWithProduct(int orderId) {
        return orderDetailDao.getOrderDetailsWithProduct(orderId);
    }

    public Map<String, String> getProductInfoByVariantId(int variantId) {
        return orderDetailDao.getProductInfoByVariantId(variantId);
    }

    public boolean addOrderDetail(OrderDetail orderDetail) {
        return orderDetailDao.addOrderDetail(orderDetail);
    }

    public double calculateOrderTotal(int orderId) {
        List<OrderDetail> details = orderDetailDao.getOrderDetailsByOrderId(orderId);
        return details.stream()
                .mapToDouble(OrderDetail::getTotalMoney)
                .sum();
    }
}