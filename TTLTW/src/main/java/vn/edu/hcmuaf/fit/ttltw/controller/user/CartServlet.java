package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.dao.ShippingDao;
import vn.edu.hcmuaf.fit.ttltw.model.Address;
import vn.edu.hcmuaf.fit.ttltw.model.ShippingZoneFees;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.model.Voucher;
import vn.edu.hcmuaf.fit.ttltw.service.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private final CartService cartService = new CartServiceImpl();
    private final OrderService orderService = new OrderService();
    private final ShippingDao shippingDao = new ShippingDao();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "view";

        switch (action) {
            case "view":
                showCart(request, response, user.getId());
                break;
            case "add":
                addToCart(request, response, user.getId());
                break;
            case "update":
                updateQuantity(request, response, user.getId());
                break;
            case "remove":
                removeItem(request, response, user.getId());
                break;
            case "clear":
                clearCart(request, response, user.getId());
                break;
            case "changeVariant":
                changeVariant(request, response, user.getId());
                break;
            case "checkout":
                checkout(request, response, user.getId());
                break;
            default:
                response.sendRedirect("home");
                break;
        }
    }
// Xử lý chuyển qua trang thanh toán
    private void checkout(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException, ServletException {

        String selectedIds = request.getParameter("selectedIds");

        if (selectedIds == null || selectedIds.isEmpty()) {
            response.sendRedirect("cart?action=view");
            return;
        }

        String[] ids = selectedIds.split(",");

        List<Map<String, Object>> allItems = cartService.getCartForDisplay(userId);
        List<Map<String, Object>> selectedItems = new ArrayList<>();

        double total = 0;
        double finalShippingFee = 0.0;

        for (Map<String, Object> item : allItems) {
            String vcId = item.get("vc_id").toString();

            for (String id : ids) {
                if (vcId.equals(id)) {
                    selectedItems.add(item);
                    total += Double.parseDouble(item.get("subtotal").toString());
                }
            }
        }
        // Lấy địa chỉ mặc định của người dùng
        Address defaultAddress = orderService.getDefaultAddress(userId);
        request.setAttribute("defaultAddress", defaultAddress);

       // lấy danh sách đơn vị vận chuyển từ DB
        if (defaultAddress != null) {
            String province = extractProvince(defaultAddress.getAddress());

            // Lấy danh sách phí dựa trên tỉnh của địa chỉ
            List<ShippingZoneFees> shippingOptions = shippingDao.getListFeesByProvince(province);
            request.setAttribute("shippingOptions", shippingOptions);

            System.out.println("DEBUG - Tỉnh của User: [" + province + "]");


            // thiết lập phí mặc định ban đầu là phí của phương thức đầu tiên
            double initialShippingFee = (shippingOptions != null && !shippingOptions.isEmpty())
                    ? shippingOptions.get(0).getBaseFee().doubleValue()
                    : 30000.0;
            finalShippingFee = initialShippingFee;
        } else {
            finalShippingFee = 0.0;
        }

        // lấy danh sách voucher đang hoạt động
        List<Voucher> availableVouchers = orderService.getActiveVouchers();
        request.setAttribute("availableVouchers", availableVouchers);
        request.setAttribute("checkoutItems", selectedItems);
        request.setAttribute("subtotal", total);
        request.setAttribute("shippingFee", finalShippingFee);

        request.getRequestDispatcher("/views/user/checkout.jsp")
                .forward(request, response);
    }

    // Hiển thị trang giỏ hàng
    private void showCart(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        List<Map<String, Object>> items = cartService.getCartForDisplay(userId);
        double total = cartService.calculateCartTotal(userId);

        request.setAttribute("cartItems", items);
        request.setAttribute("totalCartPrice", total);
        request.getRequestDispatcher("/views/user/cart.jsp").forward(request, response);
    }

    // Xử lý thêm vào giỏ
    private void addToCart(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        int vId = Integer.parseInt(request.getParameter("vcId"));
        int quantity = 1; // Mặc định mỗi lần click là 1

        boolean success = cartService.addToCart(userId, vId, quantity);

        if (success) {
            int totalCount = cartService.getTotalQuantity(userId); // lấy tổng số lượng
            response.getWriter().print(totalCount);
        } else {
            response.setStatus(400);
            response.getWriter().print("STOCK_EXCEEDED");
        }
    }
// Cập nhật số lượng
    private void updateQuantity(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        int vId = Integer.parseInt(request.getParameter("vcId"));
        int delta = Integer.parseInt(request.getParameter("delta"));
        boolean success = cartService.updateCartItem(userId, vId, delta);

        if (success) {
            double newTotal = cartService.calculateCartTotal(userId);
            String json = "{\"status\": \"success\", \"newTotal\": " + newTotal + "}";
            response.getWriter().print(json);
        } else {
            response.setStatus(400);
            response.getWriter().print("{\"status\": \"error\", \"message\": \"Hết hàng!\"}");
        }
    }

    // Xóa một món hàng
    private void removeItem(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        int vId = Integer.parseInt(request.getParameter("vcId"));
        cartService.removeCartItem(userId, vId);
        response.sendRedirect("cart?action=view");
    }
// Xóa tất cả món hàng
    private void clearCart(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        cartService.clearCart(userId);
        response.sendRedirect("cart?action=view");
    }
    // Thay đổi biến thể màu/variant
    private void changeVariant(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {

        int oldId = Integer.parseInt(request.getParameter("oldVcId"));
        int newId = Integer.parseInt(request.getParameter("newVcId"));
        boolean success = cartService.changeVariant(userId, oldId, newId);
        response.setContentType("application/json");
        if (success) {
            response.getWriter().print("{\"status\":\"success\"}");
        } else {
            response.setStatus(400);
            response.getWriter().print("{\"status\":\"error\",\"message\":\"Hết hàng\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private String extractProvince(String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank()) {
            return "";
        }

        String candidate = fullAddress.trim();
        if (candidate.contains(",")) {
            String[] parts = candidate.split(",");
            candidate = parts[parts.length - 1].trim();
        }

        return normalizeProvince(candidate);
    }

    private String normalizeProvince(String province) {
        if (province == null) {
            return "";
        }

        String result = province.trim();
        result = result.replaceFirst("(?i)^thanh pho\\s+", "");
        result = result.replaceFirst("(?i)^tp\\.?\\s*", "");
        result = result.replaceFirst("(?i)^tinh\\s+", "");
        return result.trim();
    }
}