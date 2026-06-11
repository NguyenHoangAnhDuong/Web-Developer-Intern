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
    private final OrderDetailDao orderDetailDao;
    private final VoucherAdminDao voucherDao = new VoucherAdminDaoImpl();
    private final SuperAIService superAIService = new SuperAIService();
    private final ShippingDao shippingDao = new ShippingDao();

    public OrderService() {
        this.orderDao = new OrderDao();
        this.addressDao = new AddressDao();
        this.paymentTypesDao = new PaymentTypesDao();
        this.orderDetailDao = new OrderDetailDao();
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
//
//    public int createOrder(Order order) {
//        return orderDao.createOrder(order);
//    }
//
//    public boolean updateOrderStatus(int orderId, int status) {
//        return orderDao.updateOrderStatus(orderId, status);
//    }

    public boolean cancelOrder(int orderId, int userId) {
        Map<String, Object> res = updateStatus(orderId, 4);
        Object ok = res.get("success");
        return ok instanceof Boolean && (Boolean) ok;
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
            case 0:
                return "Chờ xác nhận";
            case 1:
                return "Đang lên đơn";
            case 2:
                return "Đang giao";
            case 3:
                return "Đã giao";
            case 4:
                return "Đã hủy";
            default:
                return "Không xác định (status: " + status + ")";
        }
    }

    public static String getStatusClass(int status) {
        switch (status) {
            case 0:
                return "pending";
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
            case 0:
                return "fa-solid fa-hourglass-start";
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

        Order order = opt.get();
        int current = order.getStatus();
        System.out.println("OrderService Order " + orderId + ": " + getStatusName(current) + " -> " + getStatusName(newStatus));

        // Không thể thay đổi đơn đã kết thúc
        if (current == 3 || current == 4) {
            result.put("success", false);
            result.put("message", "Đơn hàng đã kết thúc, không thể thay đổi");
            return result;
        }

        // Chỉ cho phép các chuyển đổi hợp lệ
        // 0 -> 1 xác nhận, 0 -> 4 hủy trước xác nhận
        // 1 -> 2 bắt đầu giao, 1 -> 4 hủy trước khi giao
        // 2 -> 3 giao thành công
        boolean isValidTransition = (current == 1 && (newStatus == 2 || newStatus == 4)) ||
            (current == 2 && (newStatus == 3 || newStatus == 4)) ||
            (current == 0 && (newStatus == 1 || newStatus == 2 || newStatus == 4));
        if (!isValidTransition) {
            result.put("success", false);
            result.put("message", "Không thể chuyển từ " + getStatusName(current) + " sang " + getStatusName(newStatus));
            return result;
        }

        String tracking = null;
        boolean apiCalled = false;

        if (newStatus == 1) {
            // status 0 -> 1: tạo đơn vận chuyển trên SuperAI
            try {
                Address address = getOrderAddress(order.getAddressId());
                if (address == null) {
                    result.put("success", false);
                    result.put("message", "Đơn hàng không có địa chỉ giao hàng");
                    return result;
                }

                double codAmount = (order.getPaymentTypeId() == 1) ? order.getTotalAmount() : 0;

                tracking = superAIService.createRealOrder(
                        orderId, address.getName(), address.getPhoneNumber(),
                        address.getAddress(), codAmount
                );

                if (tracking != null && !tracking.trim().isEmpty()) {
                    shippingDao.updateTrackingInfo(orderId, tracking, resolveShippingPartner(order.getPartnerName()));
                    apiCalled = true;
                } else {
                    System.err.println("SuperAI trả về null tracking");
                }

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

        } else if (newStatus == 4) {
            String carrierCancelError = null;
            try {
                tracking = order.getTrackingNumber();
                if (tracking == null || tracking.trim().isEmpty()) {
                    tracking = shippingDao.getTrackingByOrderId(orderId);
                }

                if (tracking != null && !tracking.trim().isEmpty()) {
                    boolean cancelSuccess = superAIService.cancelOrder(tracking);
                    if (!cancelSuccess) {
                        carrierCancelError = superAIService.getLastCancelErrorMessage();
                        System.err.println( orderId + ": " + carrierCancelError );
                    } else {
                        apiCalled = true;
                    }
                } else {
                    System.out.println("không có mã tracking code trong DB cho order " + orderId );
                    String fallbackCode = "DH" + orderId;
                    try {
                        boolean cancelSuccess = superAIService.cancelOrder(fallbackCode);
                        if (!cancelSuccess) {
                            carrierCancelError = superAIService.getLastCancelErrorMessage();
                            System.err.println( orderId + ": " + carrierCancelError+ " "+ fallbackCode );
                        } else {
                            apiCalled = true;
                            shippingDao.updateTrackingInfo(orderId, fallbackCode, resolveShippingPartner(order.getPartnerName()));
                        }
                    } catch (Exception ex) {
                        carrierCancelError = ex.getMessage();
                        System.err.println( orderId + ": " + carrierCancelError);
                    }
                }

            } catch (Exception e) {
                carrierCancelError = e.getMessage();
                System.err.println( orderId + ": " + carrierCancelError);
                e.printStackTrace();
            }
            if (carrierCancelError != null && !carrierCancelError.isBlank()) {
                result.put("carrierCancelError", carrierCancelError);
            }
        }

        // Cập nhật trạng thái trong DB
        if (!orderDao.updateStatus(orderId, newStatus)) {
            result.put("success", false);
            result.put("message", "Lỗi DB: Không thể cập nhật trạng thái đơn hàng");
            return result;
        }

        System.out.println("OrderService Order " + orderId + " updated to " + getStatusName(newStatus));
        result.put("success", true);
        result.put("tracking", tracking);

        if (newStatus == 1 && !apiCalled) {
            result.put("message", "Cập nhật thành công. Lỗi gọi API vận chuyển, cần cập nhật mã vận đơn sau.");
            return result;
        }
        if (newStatus == 4 && result.containsKey("carrierCancelError")) {
            String apiMsg = (String) result.get("carrierCancelError");
            result.put("message", "Cập nhật thành công. Hủy ở đơn vị vận chuyển thất bại" + (apiMsg != null && !apiMsg.isBlank() ? ": " + apiMsg : "."));
            return result;
        }

        result.put("message", "Cập nhật thành công. Mã vận đơn: " + (tracking != null ? tracking : "N/A"));
        return result;
    }

    public int processOrder(int userId, int addressId, String paymentMethod, String voucherCode,
                            Map<Integer, Integer> cart, int paymentStatus, String buyerNote, double shippingFee,
                            String shippingPartner) {
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
        // status 0 = Chờ xác nhận
        order.setStatus(0);
        int paymentTypeId = "bank".equalsIgnoreCase(paymentMethod) ? 2 : 1;
        order.setPaymentTypeId(paymentTypeId);// Trạng thái Chờ xác nhận
        order.setFeeShipping(shippingFee);
        order.setPartnerName(resolveShippingPartner(shippingPartner));
        order.setVoucherId(appliedVoucherId);
        order.setDiscountAmount(discount);
        order.setTotalAmount(subtotal + shippingFee - discount);
        order.setNote(buyerNote);
        return orderDao.insertOrderWithDetails(order, cart);
    }

    private String resolveShippingPartner(String shippingPartner) {
        if (shippingPartner == null) {
            return "SuperAI";
        }
        String normalized = shippingPartner.trim();
        return normalized.isEmpty() ? "SuperAI" : normalized;
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
    public String getTrackingByOrderId(int orderId) {
        return shippingDao.getTrackingByOrderId(orderId);
    }
    public boolean updateStatusOnly(int orderId, int newStatus) {
        return orderDao.updateStatus(orderId, newStatus);
    }
    public Map<String, Object> getOrderDetailsForAdmin(int orderId) {
        Map<String, Object> result = new HashMap<>();
        Optional<Order> optOrder = orderDao.getOrderById(orderId);
        if (optOrder.isEmpty()) {
            result.put("success", false);
            result.put("message", "Đơn hàng không tồn tại");
            return result;
        }
        Order order = optOrder.get();
        Address address = getOrderAddress(order.getAddressId());
        // Lấy chi tiết đơn hàng
        List<OrderDetail> orderDetails = orderDetailDao.getOrderDetailsByOrderId(orderId);
        
        List<Map<String, Object>> items = new java.util.ArrayList<>();
        for (OrderDetail detail : orderDetails) {
            Map<String, String> productInfo = orderDetailDao.getProductInfoByVariantId(detail.getVariantId());
            Map<String, Object> item = new HashMap<>();
            item.put("productName", productInfo.getOrDefault("productName", "N/A"));
            item.put("variantName", productInfo.getOrDefault("variantName", ""));
            item.put("colorName", productInfo.getOrDefault("colorName", ""));
            item.put("quantity", detail.getQuantity());
            item.put("price", detail.getPrice());
            item.put("totalMoney", detail.getTotalMoney());
            items.add(item);
        }
        result.put("success", true);
        result.put("order", order);
        result.put("address", address);
        result.put("items", items);
        result.put("statusName", getStatusName(order.getStatus()));
        result.put("statusClass", getStatusClass(order.getStatus()));
        return result;
    }
//    public Map<String, Object> cancelOrderWithReason(int orderId, String cancellationReason) {
//        Map<String, Object> result = new HashMap<>();
//        Optional<Order> opt = orderDao.getOrderById(orderId);
//        if (opt.isEmpty()) {
//            result.put("success", false);
//            result.put("message", "Đơn hàng không tồn tại");
//            return result;
//        }
//        Order order = opt.get();
//        int currentStatus = order.getStatus();
//        // Kiểm tra trạng thái - chỉ có thể hủy các đơn hàng ở trạng thái 1 hoặc 2
//        if (currentStatus != 1 && currentStatus != 2) {
//            result.put("success", false);
//            result.put("message", "Không thể hủy đơn hàng ở trạng thái " + getStatusName(currentStatus));
//            return result;
//        }
//        if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
//            result.put("success", false);
//            result.put("message", "Vui lòng nhập lý do hủy đơn hàng");
//            return result;
//        }
//        // Cập nhật status sang 4 (hủy) và lưu lý do hủy
//        if (!orderDao.cancelOrderWithReason(orderId, 4, cancellationReason)) {
//            result.put("success", false);
//            result.put("message", "Lỗi DB: Không thể hủy đơn hàng");
//            return result;
//        }
//        result.put("success", true);
//        result.put("message", "Hủy đơn hàng thành công");
//        return result;
//    }

    public Map<String, Object> cancelOrderWithReason(int orderId, String cancellationReason) {
        Map<String, Object> result = new HashMap<>();
        Optional<Order> opt = orderDao.getOrderById(orderId);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "Đơn hàng không tồn tại");
            return result;
        }

        Order order = opt.get();
        int currentStatus = order.getStatus();

        if (currentStatus != 0 && currentStatus != 1) {
            result.put("success", false);
            result.put("message", "Không thể hủy đơn hàng ở trạng thái " + getStatusName(currentStatus));
            return result;
        }
        if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Vui lòng nhập lý do hủy đơn hàng");
            return result;
        }
        String carrierCancelError = null;

// chỉ status = 1 mới gọi API hủy vận chuyển
        if (currentStatus == 1) {

            String tracking = order.getTrackingNumber();

            if (tracking == null || tracking.isBlank()) {
                tracking = shippingDao.getTrackingByOrderId(orderId);
            }

            if (tracking != null && !tracking.isBlank()) {
                try {
                    boolean cancelled = superAIService.cancelOrder(tracking);

                if (!cancelled) {
                    carrierCancelError = superAIService.getLastCancelErrorMessage();
                    System.err.println("OrderService: carrier cancel failed for order " + orderId + ": " + carrierCancelError);
                }
            } catch (Exception e) {
                carrierCancelError = e.getMessage();
                e.printStackTrace();
            }
        }}

        if (!orderDao.cancelOrderWithReason(orderId, 4, cancellationReason)) {
            result.put("success", false);
            result.put("message", "Lỗi DB: Không thể hủy đơn hàng");
            return result;
        }

        result.put("success", true);
        if (carrierCancelError != null && !carrierCancelError.isBlank()) {
            result.put("message", "Hủy đơn hàng thành công. Hủy ở đơn vị vận chuyển thất bại: " + carrierCancelError);
        } else {
            result.put("message", "Hủy đơn hàng thành công");
        }
        return result;
    }
}