package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.*;
import vn.edu.hcmuaf.fit.ttltw.model.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrderService {
    private final OrderDao orderDao;
    private final AddressDao addressDao;
    private final PaymentTypesDao paymentTypesDao;
    private final VoucherAdminDao voucherDao = new VoucherAdminDaoImpl();

    public OrderService() {
        this.orderDao = new OrderDao();
        this.addressDao = new AddressDao();
        this.paymentTypesDao = new PaymentTypesDao();
    }

    public List<Order> getUserOrders(int userId) {
        return orderDao.getOrdersByUserId(userId);
    }

    public List<Order> getUserOrdersByStatus(int userId, int status) {
        return orderDao.getOrdersByUserIdAndStatus(userId, status);
    }

    public Optional<Order> getOrderById(int orderId) {
        return orderDao.getOrderById(orderId);
    }

    public int createOrder(Order order) {
        return orderDao.createOrder(order);
    }

    public boolean updateOrderStatus(int orderId, int status) {
        return orderDao.updateOrderStatus(orderId, status);
    }

    public boolean cancelOrder(int orderId, int userId) {
        return orderDao.cancelOrder(orderId, userId);
    }

    public Address getOrderAddress(int addressId) {
        return addressDao.findById(addressId).orElse(null);
    }

    public PaymentTypes getPaymentType(int paymentTypeId) {
        return paymentTypesDao.findById(paymentTypeId);
    }

    public boolean isOrderBelongToUser(int orderId, int userId) {
        Optional<Order> order = orderDao.getOrderById(orderId);
        return order.isPresent() && order.get().getUserId() == userId;
    }

    public static String getStatusName(int status) {
        switch (status) {
            case 1:
                return "Đang lên đơn";
            case 2:
                return "Đang giao";
            case 3:
                return "Đã giao";
            case 4:
                return "Đã hủy";
            default:
                return "Không xác định";
        }
    }

    public static String getStatusClass(int status) {
        switch (status) {
            case 1:
                return "prepare";
            case 2:
                return "shipping";
            case 3:
                return "delivered";
            case 4:
                return "cancelled";
            default:
                return "";
        }
    }

    public static String getStatusIcon(int status) {
        switch (status) {
            case 1:
                return "fa-solid fa-clock";
            case 2:
                return "fa-solid fa-truck";
            case 3:
                return "fa-solid fa-box";
            case 4:
                return "fa-solid fa-xmark";
            default:
                return "fa-solid fa-question";
        }
    }

    public List<Map<String, Object>> getAllForAdmin() {
        return orderDao.findAll();
    }

    public List<Map<String, Object>> searchForAdmin(String keyword, Integer status) {
        return orderDao.searchOrders(keyword, status);
    }

    public Map<String, Object> updateStatus(int orderId, int newStatus) {
        Map<String, Object> result = new HashMap<>();

        Optional<Order> opt = orderDao.getOrderById(orderId);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Đơn hàng không tồn tại");
            return result;
        }

        int current = opt.get().getStatus();

        // Không cho cập nhật nếu đã giao hoặc đã hủy
        if (current == 3) {
            result.put("success", false);
            result.put("message", "Đơn hàng đã giao, không thể thay đổi trạng thái");
            return result;
        }
        if (current == 4) {
            result.put("success", false);
            result.put("message", "Đơn hàng đã bị hủy");
            return result;
        }
        boolean isValidTransition = (current == 1 && (newStatus == 2 || newStatus == 4)) ||
                (current == 2 && (newStatus == 3 || newStatus == 4));
        if (!isValidTransition) {
            result.put("success", false);
            result.put("message",
                    "Không thể chuyển trạng thái từ "
                            + getStatusName(current) + " sang "
                            + getStatusName(newStatus));
            return result;
        }
        if (!orderDao.updateStatus(orderId, newStatus)) {
            result.put("success", false);
            result.put("message", "Lỗi hệ thống, vui lòng thử lại");
            return result;
        }
        result.put("success", true);
        result.put("message", "Cập nhật trạng thái đơn hàng thành công");
        return result;
    }

    public int processOrder(int userId, int addressId, String paymentMethod, String voucherCode,
            Map<Integer, Integer> cart, int paymentStatus,String buyerNote, double shippingFee) {
        double subtotal = 0;
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Map<String, Object> product = orderDao.getProductForCart(entry.getKey());
            if (product != null) {
                Object priceObj = product.get("unit_price");
                double unitPrice = (priceObj instanceof BigDecimal) ? ((BigDecimal) priceObj).doubleValue()
                        : (double) priceObj;
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
                double finalPrice = unitPrice * (100 - discountPercent) / 100;

                subtotal += finalPrice * entry.getValue();
            }
        }

        double discount = 0;
        Integer appliedVoucherId = null;

        if (voucherCode != null && !voucherCode.isEmpty()) {
            Voucher voucher = voucherDao.getByCode(voucherCode);
            if (voucher != null && voucher.getStatus() == 1 && subtotal >= voucher.getMinOrderValue()) {
                appliedVoucherId = voucher.getId();

                if ("percentage".equals(voucher.getType()) || "1".equals(voucher.getType())) {
                    discount = subtotal * (voucher.getDiscountAmount() / 100);
                    if (voucher.getMaxReduce() > 0 && discount > voucher.getMaxReduce()) {
                        discount = voucher.getMaxReduce();
                    }
                } else {
                    discount = voucher.getDiscountAmount();
                }
            }
        }
        Order order = new Order();
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setPaymentTypeId("bank".equals(paymentMethod) ? 2 : 1);
        order.setStatus(1);
        int paymentTypeId = "bank".equalsIgnoreCase(paymentMethod) ? 2 : 1;
        order.setPaymentTypeId(paymentTypeId);// Trạng thái Chờ xác nhận
        order.setFeeShipping(shippingFee);
        order.setVoucherId(appliedVoucherId);
        order.setDiscountAmount(discount);
        order.setTotalAmount(subtotal + shippingFee - discount);
        order.setNote(buyerNote);
        return orderDao.insertOrderWithDetails(order, cart);
    }

    public Address getDefaultAddress(int userId) {
        List<Address> addresses = orderDao.getDefaultAddressByUserId(userId);
        // Nếu list không trống, lấy phần tử đầu tiên (địa chỉ mặc định)
        if (!addresses.isEmpty()) {
            return addresses.get(0);
        }
        return null; // Trả về null để JSP hiển thị phần "Chưa có địa chỉ"
    }

    public List<Voucher> getActiveVouchers() {
        return voucherDao.getActiveVouchers();
    }

}