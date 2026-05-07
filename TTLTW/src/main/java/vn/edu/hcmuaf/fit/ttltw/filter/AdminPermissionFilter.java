package vn.edu.hcmuaf.fit.ttltw.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.utils.PermissionUtil;

import java.io.IOException;

/**
 * Lớp filter này kiểm tra quyền truy cập theo URL cho mọi request vào /admin/*.
 * Nó CHỈ chặy sau khi LoginFilter đã xác nhận user đã đăng nhập và là staff (không phải khách hàng).
 * Đối với super_admin: bỏ qua mọi check.
 * Đối với các URL liên quan đến quản lý nhân viên / vai trò: đã được kiểm tra trong servlet, filter cũng kiểm tra cho đồng nhất.
 */
@WebFilter(urlPatterns = {"/admin/*"})
public class AdminPermissionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        HttpSession session = req.getSession(false);

        // Chưa login → để LoginFilter xử lý
        if (session == null || session.getAttribute("user") == null) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        // super_admin: bỏ qua mọi kiểm tra
        if (PermissionUtil.isSuperAdmin(session)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }
        // Khách hàng: LoginFilter đã chặn /admin/*; trường hợp lọt qua coi như từ chối
        if (PermissionUtil.isCustomer(session)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
        String method = req.getMethod();
        String action = req.getParameter("action");

        String required = resolveRequiredPermission(path, method, action);

        // Không có yêu cầu permission cụ thể (ví dụ /admin/dashboard, hoặc URL không khớp) → cho qua
        if (required == null) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (PermissionUtil.getPermissions(session).contains(required)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        // Từ chối truy cập
        denyAccess(req, resp);
    }

    /**
     * Trả về tên permission được yêu cầu cho request, hoặc null nếu không cần kiểm tra.
     */
    private String resolveRequiredPermission(String path, String method, String action) {
        boolean isPost = "POST".equalsIgnoreCase(method);

        // ----- DASHBOARD -----
        if (path.equals("/admin/dashboard")) return null; // mở cho mọi staff đã đăng nhập

        // ----- PRODUCT -----
        if (path.equals("/admin/products")) {
            if (isPost) {
                // doPost hiện chỉ xử lý action=toggle (ẩn/hiện sản phẩm) → coi như update
                return "product.update";
            }
            return "product.view";
        }
        if (path.equals("/admin/product/add")) {
            return "product.create"; // cả GET (mở form) và POST (lưu)
        }
        if (path.equals("/admin/products/edit")) {
            return "product.update";
        }

        // ----- ORDER -----
        if (path.equals("/admin/orders")) {
            return isPost ? "order.update" : "order.view";
        }

        // ----- CUSTOMER -----
        if (path.equals("/admin/users")) {
            return isPost ? "customer.update" : "customer.view";
        }
        if (path.equals("/admin/customers/detail")) {
            if (isPost) {
                if ("resetPassword".equals(action)) return "customer.reset_password";
                return "customer.update";
            }
            return "customer.view";
        }

        // ----- VOUCHER -----
        if (path.equals("/admin/vouchers")) {
            if (!isPost) return "voucher.view";
            if (action == null) return "voucher.view";
            return switch (action) {
                case "addVoucher" -> "voucher.create";
                case "update", "toggle" -> "voucher.update";
                case "delete" -> "voucher.delete";
                default -> "voucher.view";
            };
        }

        // ----- FEEDBACK -----
        if (path.equals("/admin/feedbacks")) {
            // Feedback dùng GET cho cả list/approve/hide/delete
            if (action == null || action.equals("list")) return "feedback.view";
            return switch (action) {
                case "approve", "hide" -> "feedback.update";
                case "delete" -> "feedback.delete";
                default -> "feedback.view";
            };
        }

        // ----- EMPLOYEE / ROLE -----
        // Đã được kiểm tra trong servlet (EmployeeAdminServlet, RoleAdminServlet) nên không cần lặp ở đây
        return null;
    }

    private void denyAccess(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (isAjax(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"message\":\"Bạn không có quyền thực hiện hành động này\"}");
            return;
        }
        // Hiển thị toast khi user vào lại trang sau redirect
        HttpSession s = req.getSession(true);
        s.setAttribute("toastMessage", "Bạn không có quyền truy cập chức năng này");
        s.setAttribute("toastType", "error");
        // Redirect về dashboard (luôn cho phép) để tránh vòng lặp
        resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
    }

    private boolean isAjax(HttpServletRequest req) {
        String xrw = req.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(xrw)) return true;
        String accept = req.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) return true;
        String ajax = req.getParameter("ajax");
        if ("1".equals(ajax) || "true".equalsIgnoreCase(ajax)) return true;
        // POST tới các servlet trả JSON: nhận diện theo path để FE không cần đổi
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String contextPath = req.getContextPath();
            String uri = req.getRequestURI();
            String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
            return path.equals("/admin/users")
                    || path.equals("/admin/orders")
                    || path.equals("/admin/products")
                    || path.equals("/admin/employees")
                    || path.equals("/admin/roles");
        }
        return false;
    }
}
