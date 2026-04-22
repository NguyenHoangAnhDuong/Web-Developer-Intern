package vn.edu.hcmuaf.fit.ttltw.controller.admin;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.model.Feedback;
import vn.edu.hcmuaf.fit.ttltw.service.FeedBackService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
@WebServlet("/admin/feedbacks")
public class FeedbackManagementServlet extends HttpServlet {
    private FeedBackService feedBackService;
    @Override
    public void init() {
        feedBackService = new FeedBackService();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";
        switch (action) {
            case "list"    -> showList(request, response);
            case "approve" -> updateStatus(request, response, 1);
            case "hide"    -> updateStatus(request, response, 0);
            case "delete"  -> deleteFeedback(request, response);
            default        -> response.sendRedirect(request.getContextPath() + "/admin/feedbacks");
        }
    }
    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword   = request.getParameter("keyword");
        String starParam = request.getParameter("star");
        String status    = request.getParameter("status");
        Integer star = null;
        if (starParam != null && !starParam.isEmpty()) {
            try { star = Integer.parseInt(starParam); } catch (NumberFormatException ignored) {}
        }
        List<Feedback> feedbacks = feedBackService.searchFeedbacks(keyword, star, status);
        long total   = feedbacks.size();
        long showing = feedbacks.stream().filter(f -> f.getStatus() == 1).count();
        long pending = feedbacks.stream().filter(f -> f.getStatus() == 2).count();
        long hidden  = feedbacks.stream().filter(f -> f.getStatus() == 0).count();
        request.setAttribute("feedbacks", feedbacks);
        request.setAttribute("total",     total);
        request.setAttribute("showing",   showing);
        request.setAttribute("pending",   pending);
        request.setAttribute("hidden",    hidden);
        request.setAttribute("keyword",   keyword != null ? keyword : "");
        request.setAttribute("starParam", starParam != null ? starParam : "");
        request.setAttribute("status",    status != null ? status : "");
        request.setAttribute("stars", Arrays.asList(5,4,3,2,1));

        request.getRequestDispatcher("/views/admin/feedbackManagement.jsp").forward(request, response);
    }
    private void updateStatus(HttpServletRequest request, HttpServletResponse response,
                              int newStatus) throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            feedBackService.updateStatus(id, newStatus);
        } catch (NumberFormatException ignored) {}
        response.sendRedirect(request.getContextPath()
                + "/admin/feedbacks?" + buildQuery(request));
    }
    private void deleteFeedback(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            feedBackService.deleteFeedback(id);
        } catch (NumberFormatException ignored) {}
        response.sendRedirect(request.getContextPath()
                + "/admin/feedbacks?" + buildQuery(request));
    }
    private String buildQuery(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder("action=list");
        String kw = req.getParameter("keyword");
        String st = req.getParameter("star");
        String ss = req.getParameter("status");
        if (kw != null && !kw.isEmpty()) sb.append("&keyword=").append(kw);
        if (st != null && !st.isEmpty()) sb.append("&star=").append(st);
        if (ss != null && !ss.isEmpty()) sb.append("&status=").append(ss);
        return sb.toString();
    }
}