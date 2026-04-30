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
    private final SuperAIService superAIService = new SuperAIService();
    private final ShippingService shippingService = new ShippingService();
    private final ShippingDao shippingDao = new ShippingDao();

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
//
//    public int createOrder(Order order) {
//        return orderDao.createOrder(order);
//    }
//
//    public boolean updateOrderStatus(int orderId, int status) {
//        return orderDao.updateOrderStatus(orderId, status);
//    }

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
            case 14:
                return "Chờ xác nhận";
            default:
                return "Không xác định (status: " + status + ")";
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
            case 14:
                return "pending";
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
            case 14:
                return "fa-solid fa-hourglass-start";
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
            System.err.println(" OrderService  Order " + orderId + " not found");
            return result;
        }

        Order order = opt.get();
        int current = order.getStatus();
        System.out.println(" OrderService Order " + orderId + ": " + getStatusName(current) + " -> " + getStatusName(newStatus));

        //   Kiểm tra logic chuyển đổi trạng thái
        if (current == 3 || current == 4) {
            result.put("success", false);
            result.put("message", "Đơn hàng đã kết thúc (giao/hủy) , không thể thay đổi");
            System.err.println(" OrderService  Order " + orderId + " sẵn sàng ");
            return result;
        }

        boolean isValidTransition = (current == 1 && (newStatus == 2 || newStatus == 4)) ||
                (current == 2 && (newStatus == 3 || newStatus == 4)) ||
                (current == 14 && (newStatus == 1 || newStatus == 2 || newStatus == 4));
        if (!isValidTransition) {
            result.put("success", false);
            result.put("message", "Không thể chuyển từ " + getStatusName(current) + " sang " + getStatusName(newStatus));
            System.err.println(" OrderService Invalid transition: " + current + " -> " + newStatus);
            return result;
        }

        // nếu chuyển sang trạng thái 2 thì gọi superAI trước
        String tracking = null;
        boolean apiCalled = false;
        if (newStatus == 2 && (current == 1)) {
            try {
                System.out.println(" OrderService  Calling SuperAI API for order " + orderId);
                Address address = getOrderAddress(order.getAddressId());
                if (address == null) {
                    result.put("success", false);
                    result.put("message", "Lỗi: Đơn hàng không có địa chỉ giao hàng.");
                    System.err.println(" OrderService  Order " + orderId + " has no address");
                    return result;
                }

                double codAmount = (order.getPaymentTypeId() == 1) ? order.getTotalAmount() : 0;
                System.out.println(" OrderService  Address: " + address.getAddress());
                System.out.println(" OrderService  COD Amount: " + codAmount);

                // Gọi API
                tracking = superAIService.createRealOrder(
                        orderId, address.getName(), address.getPhoneNumber(),
                        address.getAddress(), codAmount
                );
                if (tracking != null) {
                    shippingDao.updateTrackingInfo(orderId, tracking, "SuperShip");
                    System.out.println("Saved tracking: " + tracking);
                }

                if (tracking == null || tracking.trim().isEmpty()) {
                    // debug ra lỗi
                    System.err.println(" OrderService  SuperAI returned null - Continuing without tracking code");
                    // KHÔNG fail ở đây, tiếp tục cập nhật status
                } else {
                    System.out.println(" OrderService  SuperAI returned tracking: " + tracking);

                    // lưu tracking vào DB trước khi đổi trạng thái đơn hàng
                    boolean trackingSaved = shippingService.updateTrackingInfo(orderId, tracking, "SuperAI");
                    if (!trackingSaved) {
                        System.err.println("OrderService  Failed to save tracking info, but continuing status update");
                    } else {
                        System.out.println(" OrderService  Tracking saved to DB successfully");
                        apiCalled = true;
                    }
                }

            } catch (Exception e) {
                System.err.println(" OrderService   Exception calling SuperAI  : " + e.getMessage());
                e.printStackTrace();
            }
        }


        // mới cập nhật trạng thái đơn hàng trong DB
        if (!orderDao.updateStatus(orderId, newStatus)) {
            result.put("success", false);
            result.put("message", "Lỗi DB: Không thể cập nhật trạng thái đơn hàng.");
            System.err.println(" OrderService  DB update failed for order " + orderId);
            return result;
        }

        System.out.println(" OrderService  Order " + orderId + " status updated to " + getStatusName(newStatus));
        result.put("success", true);
        result.put("tracking", tracking); // Trả về tracking để Admin thấy nếu cần

        if (newStatus == 2 && !apiCalled) {
            result.put("message", "Cập nhật thành công.  Lỗi gọi API vận chuyển, cần cập nhật mã vận đơn sau.");
        } else {
            result.put("message", "Cập nhật thành công. Mã vận đơn: " + (tracking != null ? tracking : "N/A"));
        }
        return result;
    }

    public int processOrder(int userId, int addressId, String paymentMethod, String voucherCode,
                            Map<Integer, Integer> cart, int paymentStatus, String buyerNote, double shippingFee) {
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

    // tách việc gọi API vận chuyển ra chạy nền
    // không block request user
    // API ship có thể chậm / lỗi
    public void handleShippingAsync(int orderId, String name, String phone, String address, double amount) {
        new Thread(() -> {
            try {
                ShippingService shippingService = new ShippingService();
                String tracking = shippingService.createShipment(
                        orderId, name, phone, address, amount
                );
                if (tracking != null) {
                    System.out.println("tracking đã được lưu " + tracking);
                } else {
                    System.err.println("orderService  không có tracking ");
                }

            } catch (Exception e) {
                System.err.println("orderService async lỗi: " + e.getMessage());
            }
        }).start();
    }

    public String getTrackingByOrderId(int orderId) {
        return shippingDao.getTrackingByOrderId(orderId);
    }
    public boolean updateStatusOnly(int orderId, int newStatus) {
        return orderDao.updateStatus(orderId, newStatus);
    }
}