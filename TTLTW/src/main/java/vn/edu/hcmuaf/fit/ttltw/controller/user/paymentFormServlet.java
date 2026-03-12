package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.service.UserService;

@WebServlet("/user/paymentForm")
public class paymentFormServlet extends HttpServlet {
    private UserService userService;


    @Override
    public void init() throws ServletException {
        this.userService = new UserService();
    }
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response){


    }
}
