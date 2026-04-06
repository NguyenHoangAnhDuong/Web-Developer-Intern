package vn.edu.hcmuaf.fit.ttltw.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.CartService;
import vn.edu.hcmuaf.fit.ttltw.service.CartServiceImpl;

import java.io.IOException;
@WebFilter("/*")
public class CartCountFilter implements Filter {
    private final CartService cartService = new CartServiceImpl();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpSession session = httpRequest.getSession(false);

        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                int totalCount = cartService.getTotalQuantity(user.getId());
                session.setAttribute("cartItemCount", totalCount);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);

    }
}
