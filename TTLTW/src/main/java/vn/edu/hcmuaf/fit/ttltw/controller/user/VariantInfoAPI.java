package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.service.CartService;
import vn.edu.hcmuaf.fit.ttltw.service.CartServiceImpl;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/variant-info")
public class VariantInfoAPI extends HttpServlet {
    private final CartService cartService = new CartServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int id = Integer.parseInt(request.getParameter("id"));

        Map<String, Object> data = cartService.getVariantInfo(id);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = "{"
                + "\"price\":" + data.get("price") + ","
                + "\"stock\":" + data.get("quantity")
                + "}";

        response.getWriter().print(json);
    }
}
