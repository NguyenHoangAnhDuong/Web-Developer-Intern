package vn.edu.hcmuaf.fit.ttltw.controller.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.UserService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdminUserServlet", urlPatterns = { "/admin/users" })
public class AdminUserServlet extends HttpServlet {

    // Service xử lý logic người dùng
    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Lấy các tham số filter từ URL
        String searchTerm = req.getParameter("search");
        String roleFilter = req.getParameter("role");
        String statusFilter = req.getParameter("status");
        String pageParam = req.getParameter("page");

        int page = 1;
        try {
            if (pageParam != null) {
                page = Integer.parseInt(pageParam);
                if (page < 1)
                    page = 1;
            }
        } catch (NumberFormatException e) {
            page = 1;
        }
        // Số dòng mỗi trang
        int pageSize = 10;
        // Lấy tổng số user (sau khi filter)
        int totalUsers = userService.countUsers(searchTerm, roleFilter, statusFilter);

        // Tính tổng số trang
        int totalPage = (int) Math.ceil((double) totalUsers / pageSize);
        if (totalPage < 1)
            totalPage = 1;

        // Đảm bảo page không vượt quá totalPage
        if (page > totalPage)
            page = totalPage;

        // Tính offset
        int offset = (page - 1) * pageSize;

        // Lấy danh sách user theo trang
        List<User> usersPage = userService.getUsersPaginated(searchTerm, roleFilter, statusFilter, offset, pageSize);

        // Nếu gọi từ AJAX
        String ajax = req.getParameter("ajax");
        if ("1".equals(ajax)) {
            resp.setContentType("application/json; charset=UTF-8");

            StringBuilder sb = new StringBuilder();
            sb.append("[");

            for (int i = 0; i < usersPage.size(); i++) {
                User u = usersPage.get(i);
                sb.append("{")
                        .append("\"id\":").append(u.getId()).append(",")
                        .append("\"username\":\"").append(escapeJson(u.getUsername())).append("\",")
                        .append("\"email\":\"").append(escapeJson(u.getEmail())).append("\",")
                        .append("\"role\":").append(u.getRole()).append(",")
                        .append("\"status\":").append(u.getStatus())
                        .append("}");
                if (i < usersPage.size() - 1)
                    sb.append(",");
            }

            sb.append("]");
            resp.getWriter().write(sb.toString());
            return;
        }

        // Forward trang JSP với dữ liệu đã filter
        req.setAttribute("users", usersPage);
        req.setAttribute("totalUsers", totalUsers);
        req.setAttribute("totalPage", totalPage);
        req.setAttribute("page", page);
        req.setAttribute("currentSearch", searchTerm != null ? searchTerm : "");
        req.setAttribute("currentRole", roleFilter != null ? roleFilter : "");
        req.setAttribute("currentStatus", statusFilter != null ? statusFilter : "");

        RequestDispatcher rd = req.getRequestDispatcher("/views/admin/userManagement.jsp");
        rd.forward(req, resp);
    }

    /**
     * Xử lý POST: Cập nhật role và status của user
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int id = Integer.parseInt(req.getParameter("id"));
        int role = Integer.parseInt(req.getParameter("role"));
        int status = Integer.parseInt(req.getParameter("status"));

        boolean ok = userService.updateUser(id, role, status);

        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write("{\"success\": " + ok + "}");
    }

    // Escape ký tự đặc biệt để tránh lỗi khi trả về JSON
    private String escapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}