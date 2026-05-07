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
 * Filter kiểm tra permission theo URL cho mọi request /admin/*.
 *
 * THỨ TỰ FILTER: Servlet 6 spec không guarantee thứ tự giữa LoginFilter và filter này
 * vì cả hai đều dùng @WebFilter cùng URL pattern /admin/*. Logic dưới đây được THIẾT KẾ
 * order-independent:
 *   - Nếu chạy TRƯỚC LoginFilter: session null / customer → chain.doFilter để LoginFilter xử lý.
 *   - Nếu chạy SAU LoginFilter: các trường hợp đó không tới được filter này.
 * Cả hai chiều đều cho hành vi đúng. KHÔNG được phá vỡ tính chất này khi sửa filter.
 *
 * Các URL nhân viên / vai trò (/admin/employees, /admin/roles) được kiểm tra trong servlet
 * (EmployeeAdminServlet, RoleAdminServlet) — filter này SKIP cho chúng.
 */
@WebFilter(urlPatterns = {"/admin/*"})
public class AdminPermissionFilter implements Filter {

    /** Sentinel: nếu resolveRequired trả về mảng này → từ chối (không quyền nào hợp lệ). */
    private static final String[] DENY = new String[0];
    /** Sentinel: bỏ qua check (URL không thuộc phạm vi). */
    private static final String[] SKIP = null;

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
        // CHÚ Ý: req.getParameter() với multipart/form-data sẽ TRIGGER parse body trước khi
        // servlet kịp đọc các Part. Hiện tại các route multipart (ProductAddEServlet) KHÔNG
        // dùng "action" param, nên path-based check vẫn chạy đúng. Khi thêm route multipart
        // mới có "action", phải route bằng path/query thay vì gọi getParameter ở đây.
        String action = req.getParameter("action");

        String[] required = resolveRequiredPermission(path, method, action);

        // SKIP (null) → URL không thuộc phạm vi quản lý của filter, cho qua
        if (required == SKIP) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        // DENY (mảng rỗng) → từ chối ngay (vd action không hợp lệ)
        if (required.length == 0) {
            denyAccess(req, resp);
            return;
        }

        // Có ít nhất 1 permission khớp → cho qua
        java.util.Set<String> userPerms = PermissionUtil.getPermissions(session);
        for (String p : required) {
            if (userPerms.contains(p)) {
                chain.doFilter(servletRequest, servletResponse);
                return;
            }
        }

        // Từ chối truy cập
        denyAccess(req, resp);
    }

    /**
     * Trả về danh sách permission yêu cầu (hợp với "any of"):
     *   - SKIP (null): bỏ qua check
     *   - DENY (mảng rỗng): từ chối ngay
     *   - { "x" }: cần permission x
     *   - { "x", "y" }: cần x hoặc y
     */
    private String[] resolveRequiredPermission(String path, String method, String action) {
        boolean isPost = "POST".equalsIgnoreCase(method);

        // ----- DASHBOARD -----
        if (path.equals("/admin/dashboard")) return SKIP; // mở cho mọi staff đã đăng nhập

        // ----- PRODUCT -----
        if (path.equals("/admin/products")) {
            // doPost hiện chỉ xử lý action=toggle → coi như update
            return isPost ? new String[]{"product.update"} : new String[]{"product.view"};
        }
        if (path.equals("/admin/product/add")) {
            return new String[]{"product.create"};
        }
        if (path.equals("/admin/products/edit")) {
            return new String[]{"product.update"};
        }

        // ----- ORDER -----
        if (path.equals("/admin/orders")) {
            return isPost ? new String[]{"order.update"} : new String[]{"order.view"};
        }

        // ----- CUSTOMER -----
        if (path.equals("/admin/users")) {
            return isPost ? new String[]{"customer.update"} : new String[]{"customer.view"};
        }
        if (path.equals("/admin/customers/detail")) {
            if (isPost) {
                if ("resetPassword".equals(action)) return new String[]{"customer.reset_password"};
                return new String[]{"customer.update"};
            }
            return new String[]{"customer.view"};
        }

        // ----- VOUCHER -----
        if (path.equals("/admin/vouchers")) {
            if (!isPost) return new String[]{"voucher.view"};
            if (action == null) return DENY;
            return switch (action) {
                case "addVoucher" -> new String[]{"voucher.create"};
                case "update", "toggle" -> new String[]{"voucher.update"};
                case "delete" -> new String[]{"voucher.delete"};
                default -> DENY;
            };
        }

        // ----- FEEDBACK -----
        if (path.equals("/admin/feedbacks")) {
            // list (hoặc null action) cho phép nếu user có BẤT KỲ permission feedback nào
            // → user chỉ có feedback.delete vẫn xem được list để chọn item xóa
            if (action == null || action.equals("list")) {
                return new String[]{"feedback.view", "feedback.update", "feedback.delete"};
            }
            return switch (action) {
                case "approve", "hide" -> new String[]{"feedback.update"};
                case "delete" -> new String[]{"feedback.delete"};
                default -> DENY;
            };
        }

        // ----- EMPLOYEE / ROLE -----
        // Đã được kiểm tra trong servlet (EmployeeAdminServlet, RoleAdminServlet)
        return SKIP;
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
