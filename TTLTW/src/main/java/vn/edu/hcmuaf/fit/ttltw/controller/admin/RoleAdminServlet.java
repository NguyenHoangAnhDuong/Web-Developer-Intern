package vn.edu.hcmuaf.fit.ttltw.controller.admin;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.Permission;
import vn.edu.hcmuaf.fit.ttltw.model.Role;
import vn.edu.hcmuaf.fit.ttltw.service.RoleService;
import vn.edu.hcmuaf.fit.ttltw.utils.PermissionUtil;

import java.io.IOException;
import java.util.*;

@WebServlet(name = "RoleAdminServlet", urlPatterns = {"/admin/roles"})
public class RoleAdminServlet extends HttpServlet {

    private final RoleService roleService = new RoleService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!canManage(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không có quyền truy cập");
            return;
        }

        // AJAX: lấy permission đã gán cho 1 role
        if ("permissions".equals(req.getParameter("action"))) {
            resp.setContentType("application/json; charset=UTF-8");
            int roleId = parseInt(req.getParameter("roleId"), 0);
            resp.getWriter().write(gson.toJson(roleService.getRolePermissionIds(roleId)));
            return;
        }

        List<Role> roles = roleService.getAllRoles();
        List<Permission> permissions = roleService.getAllPermissions();

        // Gom permissions theo module
        Map<String, List<Permission>> grouped = new LinkedHashMap<>();
        for (Permission p : permissions) {
            grouped.computeIfAbsent(p.getModule(), k -> new ArrayList<>()).add(p);
        }

        req.setAttribute("roles", roles);
        req.setAttribute("permissionsByModule", grouped);
        req.getRequestDispatcher("/views/admin/roleManagement.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        if (!canManage(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"success\":false,\"message\":\"Không có quyền\"}");
            return;
        }

        String action = req.getParameter("action");
        Map<String, Object> result = new HashMap<>();
        try {
            if ("create".equals(action)) {
                String name = req.getParameter("name");
                String displayName = req.getParameter("displayName");
                String desc = req.getParameter("description");
                List<Integer> perms = parseIds(req.getParameterValues("permissionIds"));
                int id = roleService.createRole(name, displayName, desc, perms);
                result.put("success", true);
                result.put("id", id);
                result.put("message", "Đã tạo vai trò");
            } else if ("update".equals(action)) {
                int id = parseInt(req.getParameter("id"), 0);
                String displayName = req.getParameter("displayName");
                String desc = req.getParameter("description");
                List<Integer> perms = parseIds(req.getParameterValues("permissionIds"));
                boolean ok = roleService.updateRole(id, displayName, desc, perms);
                result.put("success", ok);
                result.put("message", ok ? "Đã cập nhật vai trò" : "Không thể cập nhật");
            } else if ("delete".equals(action)) {
                int id = parseInt(req.getParameter("id"), 0);
                boolean ok = roleService.deleteRole(id);
                result.put("success", ok);
                result.put("message", ok ? "Đã xoá" : "Không thể xoá (vai trò hệ thống hoặc đang được sử dụng)");
            } else {
                result.put("success", false);
                result.put("message", "Hành động không hợp lệ");
            }
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi hệ thống: " + e.getMessage());
        }
        resp.getWriter().write(gson.toJson(result));
    }

    private boolean canManage(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        // Chỉ super_admin được quản lý vai trò & phân quyền
        return s != null && PermissionUtil.isSuperAdmin(s);
    }

    private List<Integer> parseIds(String[] arr) {
        List<Integer> list = new ArrayList<>();
        if (arr == null) return list;
        for (String s : arr) {
            try { list.add(Integer.parseInt(s)); } catch (Exception ignore) {}
        }
        return list;
    }

    private int parseInt(String s, int d) {
        try { return Integer.parseInt(s); } catch (Exception e) { return d; }
    }
}
