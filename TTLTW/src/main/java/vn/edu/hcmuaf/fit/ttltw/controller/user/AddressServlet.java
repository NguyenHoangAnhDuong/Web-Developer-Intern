package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.*;
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

        boolean ok = false;

        if ("add".equals(action)) {
            Address a = buildAddress(req, userId);
            ok = service.add(a) > 0;
        }
        if ("update".equals(action)) {
            Address a = buildAddress(req, userId);
            a.setId(Integer.parseInt(req.getParameter("id")));
            ok = service.update(a);
        }
        if ("delete".equals(action)) {
            ok = service.delete(
                    Integer.parseInt(req.getParameter("id")), userId);
        }
        if ("set-default".equals(action)) {
            ok = service.setDefault(
                    Integer.parseInt(req.getParameter("id")), userId);
        }

        resp.getWriter().print("{\"success\":" + ok + "}");
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