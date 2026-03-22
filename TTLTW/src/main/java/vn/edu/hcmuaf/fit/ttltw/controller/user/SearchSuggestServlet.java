package vn.edu.hcmuaf.fit.ttltw.controller.user;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.service.ProductService;
import vn.edu.hcmuaf.fit.ttltw.service.ProductServiceImpl;

import java.io.IOException;
import java.util.List;
@WebServlet("/search-suggest")
public class SearchSuggestServlet extends HttpServlet {
    private final ProductService productService = new ProductServiceImpl();
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String keyword = request.getParameter("q");

        List<String> results = productService.getSuggestions(keyword);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        new Gson().toJson(results, response.getWriter());
    }
}
