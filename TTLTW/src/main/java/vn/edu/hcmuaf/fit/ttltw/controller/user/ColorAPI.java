package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.service.CartService;
import vn.edu.hcmuaf.fit.ttltw.service.CartServiceImpl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
@WebServlet("/api/colors-by-variant")
public class ColorAPI extends HttpServlet {
    private final CartService cartService = new CartServiceImpl();
    protected void doGet(HttpServletRequest request , HttpServletResponse response)
            throws IOException {

        int variantId = Integer.parseInt(request.getParameter("variantId"));

        List<Map<String, Object>> colors = cartService.getColorsByVariant(variantId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < colors.size(); i++) {
            Map<String, Object> c = colors.get(i);

            json.append("{")
                    .append("\"id\":").append(c.get("id")).append(",")
                    .append("\"color_name\":\"").append(c.get("color_name")).append("\"")
                    .append("}");

            if (i < colors.size() - 1) json.append(",");
        }
        json.append("]");

        response.getWriter().print(json.toString());

    }
}
