package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.*;
import vn.edu.hcmuaf.fit.ttltw.validation.AddressValidator;
import vn.edu.hcmuaf.fit.ttltw.validation.AddressValidator.ValidationResult;
import vn.edu.hcmuaf.fit.ttltw.service.AddressService;
import vn.edu.hcmuaf.fit.ttltw.service.UserService;
import vn.edu.hcmuaf.fit.ttltw.utils.SidebarUtil;

import java.io.IOException;

@WebServlet("/user/addresses")
public class AddressServlet extends HttpServlet {
    private AddressService service;
    private UserService userService;
    @Override
    public void init() {
        service = new AddressService();
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User freshUser = userService.getUserProfileById(user.getId()).orElse(user);
        session.setAttribute("user", freshUser);
        String avatarPath = freshUser.getAvatar();
        if (avatarPath == null || avatarPath.trim().isEmpty()) {
            avatarPath = req.getContextPath() + "/asset/img/admin.jpg";
        }
        int userId = freshUser.getId();
        req.setAttribute("addresses", service.getAll(userId));
        // Set sidebar data
        req.setAttribute("user", freshUser);
        req.setAttribute("activeMenu", "address");
        SidebarUtil.setSidebarData(req);
        req.setAttribute("avatarPath", avatarPath);
        req.getRequestDispatcher("/views/user/addresses.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.getWriter().print("{\"success\":false}");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        int userId = user.getId();

        String action = req.getParameter("action");
        if ("add".equals(action) || "update".equals(action)) {
            String errorJson = validateRequest(req);
            if (errorJson != null) {
                resp.getWriter().print(errorJson);
                return;
            }
        }
        boolean ok = false;

        if ("add".equals(action)) {
            Address a = buildAddress(req, userId);
            ok = service.add(a) > 0;
        }
        if ("update".equals(action)) {
            String idParam = req.getParameter("id");
            if (idParam == null || !idParam.matches("^[0-9]+$")) {
                resp.getWriter().print("{\"success\":false,\"message\":\"ID không hợp lệ.\"}");
                return;
            }
            Address a = buildAddress(req, userId);
            a.setId(Integer.parseInt(idParam));
            ok = service.update(a);
        }
        if ("delete".equals(action)) {
            String idParam = req.getParameter("id");
            if (idParam == null || !idParam.matches("^[0-9]+$")) {
                resp.getWriter().print("{\"success\":false,\"message\":\"ID không hợp lệ.\"}");
                return;
            }
            ok = service.delete(Integer.parseInt(idParam), userId);
        }
        if ("set-default".equals(action)) {
            String idParam = req.getParameter("id");
            if (idParam == null || !idParam.matches("^[0-9]+$")) {
                resp.getWriter().print("{\"success\":false,\"message\":\"ID không hợp lệ.\"}");
                return;
            }
            ok = service.setDefault(Integer.parseInt(idParam), userId);
        }

        resp.getWriter().print("{\"success\":" + ok + "}");
    }
    private String validateRequest(HttpServletRequest req) {
        ValidationResult nameResult = AddressValidator.validateName(req.getParameter("name"));
        if (!nameResult.ok) return errorJson(nameResult.message);
        ValidationResult phoneResult = AddressValidator.validatePhone(req.getParameter("phoneNumber"));
        if (!phoneResult.ok) return errorJson(phoneResult.message);
        ValidationResult addrResult = AddressValidator.validateAddress(req.getParameter("fullAddress"));
        if (!addrResult.ok) return errorJson(addrResult.message);
        return null;
    }
    private String errorJson(String message) {
        // Escape dấu nháy kép trong message để JSON hợp lệ
        String safe = message.replace("\"", "\\\"");
        return "{\"success\":false,\"message\":\"" + safe + "\"}";
    }
    private Address buildAddress(HttpServletRequest r, int userId) {
        Address a = new Address();
        a.setUserId(userId);
        a.setName(r.getParameter("name"));
        a.setPhoneNumber(r.getParameter("phoneNumber"));
        a.setAddress(r.getParameter("fullAddress"));
        a.setStatus("1".equals(r.getParameter("status")) ? 1 : 0);
        return a;
    }

}