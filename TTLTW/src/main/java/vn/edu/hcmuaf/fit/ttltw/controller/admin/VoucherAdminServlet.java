package vn.edu.hcmuaf.fit.ttltw.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.dao.VoucherAdminDaoImpl;
import vn.edu.hcmuaf.fit.ttltw.model.Voucher;
import vn.edu.hcmuaf.fit.ttltw.service.VoucherAdminService;
import vn.edu.hcmuaf.fit.ttltw.service.VoucherAdminServiceImpl;
import vn.edu.hcmuaf.fit.ttltw.validation.ValidationException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet(name ="VoucherAdminController",urlPatterns = {"/admin/vouchers"})
public class VoucherAdminServlet extends HttpServlet {

    private VoucherAdminService service;

    @Override
    public void init() {
        service = new VoucherAdminServiceImpl(new VoucherAdminDaoImpl());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String keyword = req.getParameter("keyword");
        String statusParam = req.getParameter("status");
        String pageParam = req.getParameter("page");

        Integer status = null;
        if (statusParam != null && !statusParam.isBlank() && !statusParam.equals("-1")) {
            status = Integer.parseInt(statusParam);
        }

        int page = (pageParam == null) ? 1 : Integer.parseInt(pageParam);
        int limit = 10;

        int totalVoucher = service.countAll(keyword, status);
        int totalPage = (int) Math.ceil((double) totalVoucher / limit);

        List<Voucher> vouchers = service.getAll(keyword, status, page, limit);

        req.setAttribute("vouchers", vouchers);
        req.setAttribute("totalVoucher", totalVoucher);
        req.setAttribute("totalPage", totalPage);
        req.setAttribute("page", page);
        req.setAttribute("limit", limit);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);

        req.getRequestDispatcher("/views/admin/vouchersAdmin.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        HttpSession session = req.getSession();

        try {
            if (action == null || action.isBlank()) {
                throw new ValidationException("Action không được xác định");
            }
            switch (action) {

                case "addVoucher" -> {
                    Voucher v = buildVoucher(req);
                    v.setStatus(1);
                    service.createVoucher(v);
                    session.setAttribute("toastMessage", "Thêm voucher thành công");
                    session.setAttribute("toastType", "success");
                }
                case "update" -> {
                    int id = parseInt(req, "id");
                    Voucher old = service.getById(id);
                    if (old == null) {
                        throw new ValidationException("Không tìm thấy voucher với ID: " + id);
                    }
                    Voucher v = buildVoucher(req);
                    v.setId(old.getId());
                    v.setStatus(old.getStatus());
                    service.updateVoucher(v);
                    session.setAttribute("toastMessage", "Cập nhật voucher thành công");
                    session.setAttribute("toastType", "success");
                }
                case "toggle" -> {
                    service.toggleStatus(parseInt(req, "id"));
                    session.setAttribute("toastMessage", "Cập nhật trạng thái voucher thành công");
                    session.setAttribute("toastType", "success");
                }
                case "delete" -> {
                    service.deleteVoucher(parseInt(req, "id"));
                    session.setAttribute("toastMessage", "Xóa voucher thành công");
                    session.setAttribute("toastType", "success");
                }
                default -> throw new ValidationException("Action không hợp lệ: " + action);

            }
        } catch (ValidationException e) {
            session.setAttribute("toastMessage", e.getMessage());
            session.setAttribute("toastType", "error");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/vouchers");
    }

    private Voucher buildVoucher(HttpServletRequest req) {
        Voucher v = new Voucher();

        v.setVoucherCode(req.getParameter("voucherCode"));
        v.setDiscountAmount(parseInt(req, "discountAmount"));
        v.setType(parseInt(req, "type"));
        v.setQuantity(parseInt(req, "quantity"));
        v.setMinOrderValue(parseInt(req, "minOrderValue"));
        v.setMaxReduce(parseInt(req, "maxReduce"));
        v.setStartDate(parseDate(req, "startDate"));
        v.setEndDate(parseDate(req, "endDate"));

        return v;
    }
    private int parseInt(HttpServletRequest req, String name) {
        try {
            return Integer.parseInt(req.getParameter(name));
        } catch (Exception e) {
            throw new ValidationException(name + " không hợp lệ");
        }
    }
    private double parseDouble(HttpServletRequest req, String name) {
        try {
            return Double.parseDouble(req.getParameter(name));
        } catch (Exception e) {
            throw new ValidationException(name + " không hợp lệ");
        }
    }

    private LocalDateTime parseDate(HttpServletRequest req, String name) {
        try {
            String value = req.getParameter(name);
            if (value == null || value.isBlank()) return null;

            LocalDate date = LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
            return date.atStartOfDay();

        } catch (Exception e) {
            throw new ValidationException(name + " không hợp lệ");
        }
    }
}