package vn.edu.hcmuaf.fit.ttltw.controller.admin;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.Role;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.EmployeeService;
import vn.edu.hcmuaf.fit.ttltw.service.RoleService;
import vn.edu.hcmuaf.fit.ttltw.utils.PermissionUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "EmployeeAdminServlet", urlPatterns = {"/admin/employees"})
public class EmployeeAdminServlet extends HttpServlet {

    private final EmployeeService employeeService = new EmployeeService();
    private final RoleService roleService = new RoleService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!canView(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền truy cập");
            return;
        }

        String search = req.getParameter("search");
        int page = parseInt(req.getParameter("page"), 1);
        int pageSize = 10;
        int total = employeeService.countEmployees(search);
        int totalPage = Math.max(1, (int) Math.ceil((double) total / pageSize));
        if (page > totalPage) page = totalPage;
        int offset = (page - 1) * pageSize;

        List<User> employees = employeeService.listEmployees(search, offset, pageSize);
        List<Role> roles = roleService.getAssignableRoles();

        req.setAttribute("employees", employees);
        req.setAttribute("roles", roles);
        req.setAttribute("total", total);
        req.setAttribute("totalPage", totalPage);
        req.setAttribute("page", page);
        req.setAttribute("currentSearch", search != null ? search : "");
        req.getRequestDispatcher("/views/admin/employeeManagement.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String action = req.getParameter("action");

        Map<String, Object> result = new HashMap<>();
        try {
            if ("create".equals(action)) {
                if (!canCreate(req)) { resp.setStatus(403); resp.getWriter().write(gson.toJson(err("Không có quyền tạo"))); return; }
                String fullName = req.getParameter("fullName");
                String username = req.getParameter("username");
                String email    = req.getParameter("email");
                String password = req.getParameter("password");
                int rolesId     = parseInt(req.getParameter("rolesId"), 0);
                int id = employeeService.createEmployee(fullName, username, email, password, rolesId);
                result.put("success", true);
                result.put("id", id);
                result.put("message", "Tạo tài khoản nhân viên thành công");
            } else if ("update".equals(action)) {
                if (!canUpdate(req)) { resp.setStatus(403); resp.getWriter().write(gson.toJson(err("Không có quyền cập nhật"))); return; }
                int id       = parseInt(req.getParameter("id"), 0);
                int rolesId  = parseInt(req.getParameter("rolesId"), 0);
                int status   = parseInt(req.getParameter("status"), 1);
                boolean ok = employeeService.updateEmployee(id, rolesId, status);
                result.put("success", ok);
                result.put("message", ok ? "Đã cập nhật" : "Không cập nhật được");
            } else {
                result.put("success", false);
                result.put("message", "Hành động không hợp lệ");
            }
        } catch (EmployeeService.ValidationError e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi hệ thống: " + e.getMessage());
        }
        resp.getWriter().write(gson.toJson(result));
    }

    private boolean canView(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s != null && (PermissionUtil.isSuperAdmin(s)
                || PermissionUtil.hasAny(s, "employee.view", "employee.create", "employee.update"));
    }
    private boolean canCreate(HttpServletRequest req) {
        return PermissionUtil.has(req, "employee.create");
    }
    private boolean canUpdate(HttpServletRequest req) {
        return PermissionUtil.has(req, "employee.update")
                || PermissionUtil.has(req, "employee.assign_role");
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
    private Map<String, Object> err(String m) {
        Map<String, Object> x = new HashMap<>();
        x.put("success", false); x.put("message", m);
        return x;
    }
}
