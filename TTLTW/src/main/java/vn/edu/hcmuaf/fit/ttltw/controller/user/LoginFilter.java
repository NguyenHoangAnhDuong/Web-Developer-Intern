package vn.edu.hcmuaf.fit.ttltw.controller.user;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.User;

@WebFilter(urlPatterns = {"/cart", "/checkout", "/profile", "/user/*", "/admin/*"})
public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(false);

        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();

        // Nếu CHƯA LOGIN
        if (session == null || session.getAttribute("user") == null) {

            // Lưu lại đường dẫn người dùng đang cố vào
            String queryString = request.getQueryString();
            String fullUrl = requestURI + (queryString != null ? "?" + queryString : "");
            request.getSession(true).setAttribute("redirectUrl", fullUrl);

            // Chuyển hướng sang trang login
            request.getRequestDispatcher("/login").forward(request, response);
            return;
        }

        // Đã login → kiểm tra role
        User user = (User) session.getAttribute("user");
        if (user == null) {
            // Nếu user null, xử lý như chưa login
            String queryString = request.getQueryString();
            String relativePath = requestURI;
            if (requestURI.startsWith(contextPath)) {
                relativePath = requestURI.substring(contextPath.length());
            }
            String fullUrl = relativePath + (queryString != null ? "?" + queryString : "");
            request.getSession(true).setAttribute("redirectUrl", fullUrl);
            request.getRequestDispatcher("/login").forward(request, response);
            return;
        }
        int role = user.getRolesId(); // 1 = admin, 2 = user
        // Kiểm tra phân quyền
        if (requestURI.startsWith(contextPath + "/admin") && role != 1) {
            // User thường không vào admin được
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này!");
            return;
        }
        if ((requestURI.startsWith(contextPath + "/cart")
                || requestURI.startsWith(contextPath + "/user")
                || requestURI.startsWith(contextPath + "/checkout")) && role == 1) {
            // Admin không vào trang user
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này!");
            return;
        }
        // Được phép → tiếp tục
        chain.doFilter(servletRequest, servletResponse);
    }
}
