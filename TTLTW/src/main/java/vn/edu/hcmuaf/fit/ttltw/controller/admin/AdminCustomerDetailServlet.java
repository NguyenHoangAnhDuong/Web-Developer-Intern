package vn.edu.hcmuaf.fit.ttltw.controller.admin;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.dao.UserDao;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.UserService;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "AdminCustomerDetailServlet", urlPatterns = {"/admin/customers/detail"})
public class AdminCustomerDetailServlet extends HttpServlet {

    private UserDao userDao;
    private UserService userService;

    @Override
    public void init() throws ServletException {
        userDao = UserDao.getInstance();
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        Optional<User> customer = userDao.findCustomerDetailById(id);
        if (customer.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        req.setAttribute("customer", customer.get());

        String saved = req.getParameter("saved");
        if ("1".equals(saved)) {
            req.setAttribute("savedSuccess", true);
        }

        String pwdResult = req.getParameter("pwd");
        if ("1".equals(pwdResult)) {
            req.setAttribute("passwordResetSuccess", true);
        } else if ("0".equals(pwdResult)) {
            req.setAttribute("passwordResetError", req.getParameter("msg"));
        }

        RequestDispatcher rd = req.getRequestDispatcher("/views/admin/customerDetail.jsp");
        rd.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        int id = Integer.parseInt(req.getParameter("id"));
        String action = req.getParameter("action");

        if ("resetPassword".equals(action)) {
            String newPassword = req.getParameter("newPassword");
            String result = userService.resetPasswordByAdmin(id, newPassword);
            boolean ok = "Cập nhật mật khẩu thành công".equals(result);
            String redirect = req.getContextPath() + "/admin/customers/detail?id=" + id
                    + (ok ? "&pwd=1"
                          : "&pwd=0&msg=" + java.net.URLEncoder.encode(result, java.nio.charset.StandardCharsets.UTF_8));
            resp.sendRedirect(redirect);
            return;
        }

        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String email = req.getParameter("email");

        userService.updateUserInfo(id, firstName, lastName, email);

        resp.sendRedirect(req.getContextPath() + "/admin/customers/detail?id=" + id + "&saved=1");
    }
}
