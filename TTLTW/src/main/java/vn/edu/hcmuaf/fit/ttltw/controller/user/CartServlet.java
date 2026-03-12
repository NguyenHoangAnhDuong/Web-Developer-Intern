package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.Address;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.model.Voucher;
import vn.edu.hcmuaf.fit.ttltw.service.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private final ProductService productService = new ProductServiceImpl();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute("cart", cart);
        }

        String action = request.getParameter("action");
        if (action == null) action = "view";

        switch (action) {
            case "add":
                addToCart(request, response, cart, session);
                break;
            case "remove":
                removeFromCart(request, response, cart, session);
                break;
            case "update":
                updateQuantity(request, response, cart, session);
                break;
            case "view":
                showCartPage(request, response, cart);
                break;
            case "checkout":
                prepareCheckoutPage(request, response, cart, session);
                break;
            default:
                response.sendRedirect("home");
                break;
        }
    }

    private void addToCart(HttpServletRequest request, HttpServletResponse response, Map<Integer, Integer> cart, HttpSession session) throws IOException {
        User user = (User) session.getAttribute("user");
        // NẾU CHƯA ĐĂNG NHẬP
        if (user == null) {
            response.reset(); // Xóa bỏ mọi thứ định gửi đi
            response.setStatus(401); // Gửi mã 401 để JS biết là chưa login
            response.setContentType("text/plain");
            response.getWriter().print("LOGIN_REQUIRED");
            return;
        }
        // NẾU ĐÃ ĐĂNG NHẬP THÌ MỚI CHẠY TIẾP
        try {
            String vcIdParam = request.getParameter("vcId");
            int vcId = Integer.parseInt(vcIdParam);
            cart.put(vcId, cart.getOrDefault(vcId, 0) + 1);
            syncCartSession(session, cart);
            response.setContentType("text/plain");
            response.getWriter().print(session.getAttribute("cartItemCount"));
        } catch (Exception e) {
            response.setStatus(400);
        }
    }
    private void removeFromCart(HttpServletRequest request, HttpServletResponse response, Map<Integer, Integer> cart, HttpSession session) throws IOException {
        int vcId = Integer.parseInt(request.getParameter("vcId"));
        cart.remove(vcId);
        syncCartSession(session, cart);
        response.sendRedirect("cart?action=view");
    }
    private void updateQuantity(HttpServletRequest request, HttpServletResponse response, Map<Integer, Integer> cart, HttpSession session) throws IOException {
        int vcId = Integer.parseInt(request.getParameter("vcId"));
        int delta = Integer.parseInt(request.getParameter("delta"));
        int newQty = cart.getOrDefault(vcId, 0) + delta;

        if (newQty > 0) cart.put(vcId, newQty);
        else cart.remove(vcId);

        syncCartSession(session, cart);
        response.sendRedirect("cart?action=view");
    }

    private void showCartPage(HttpServletRequest request, HttpServletResponse response, Map<Integer, Integer> cart) throws ServletException, IOException {
        List<Map<String, Object>> displayList = getCartDetails(cart);
        double total = displayList.stream().mapToDouble(i -> (double) i.get("subTotal")).sum();

        request.setAttribute("cartItems", displayList);
        request.setAttribute("totalCartPrice", total);
        request.getRequestDispatcher("/views/user/cart.jsp").forward(request, response);
    }

    private void prepareCheckoutPage(HttpServletRequest request, HttpServletResponse response, Map<Integer, Integer> cart, HttpSession session) throws ServletException, IOException {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }
        // Kiểm tra giỏ hàng tổng
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect("cart?action=view&error=empty_cart");
            return;
        }
        //  Lấy danh sách ID được chọn từ URL
        String selectedIdsParam = request.getParameter("selectedIds");
        if (selectedIdsParam == null || selectedIdsParam.isEmpty()) {
            response.sendRedirect("cart?action=view&error=no_items");
            return;
        }

        Map<Integer, Integer> checkoutCart = new LinkedHashMap<>();
        String[] ids = selectedIdsParam.split(",");
        for (String idStr : ids) {
            try {
                int id = Integer.parseInt(idStr.trim());
                if (cart.containsKey(id)) {
                    checkoutCart.put(id, cart.get(id));
                }
            } catch (NumberFormatException e) {
                System.out.println("Lỗi định dạng ID: " + idStr);
            }
        }
        if (checkoutCart.isEmpty()) {
            response.sendRedirect("cart?action=view&error=invalid_selection");
            return;
        }
try{
    List<Map<String, Object>> displayList = getCartDetails(checkoutCart);
        double subtotal = displayList.stream().mapToDouble(i -> (double) i.get("subTotal")).sum();

        Address defaultAddress = orderService.getDefaultAddress(user.getId());

        List<Voucher> availableVouchers = orderService.getActiveVouchers();
        request.setAttribute("availableVouchers", availableVouchers);
        request.setAttribute("cartItems", displayList);
        request.setAttribute("subtotal", subtotal);
        request.setAttribute("defaultAddress", defaultAddress);
        request.setAttribute("availableVouchers", availableVouchers);
        request.setAttribute("shippingFee", 30000.0);
        request.setAttribute("discountAmount", 0.0);

        request.getRequestDispatcher("/views/user/checkout.jsp").forward(request, response);
    } catch (Exception e) {
        System.out.println("LỖI TẠI PREPARE CHECKOUT: " + e.getMessage());
        e.printStackTrace();
        response.sendRedirect("cart?action=view&error=system_error");
    }}
    private List<Map<String, Object>> getCartDetails(Map<Integer, Integer> cart) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Map<String, Object> item = productService.getProductForCart(entry.getKey());
            if (item != null) {
                int qty = entry.getValue();
                // Lấy giá bán thực tế (đã tính giảm giá)
                double price = ((BigDecimal) item.get("price_final")).doubleValue();
                item.put("quantity", qty);
                item.put("subTotal", price * qty);
                item.put("vc_id", entry.getKey());
                list.add(item);
            }
        }
        return list;
    }

    private void syncCartSession(HttpSession session, Map<Integer, Integer> cart) {
        int totalQuantity = cart.values().stream().mapToInt(Integer::intValue).sum();
        session.setAttribute("cart", cart);
        session.setAttribute("cartItemCount", totalQuantity);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}