package vn.edu.hcmuaf.fit.ttltw.controller.user;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.model.Voucher;
import vn.edu.hcmuaf.fit.ttltw.service.VoucherAdminService;
import vn.edu.hcmuaf.fit.ttltw.service.VoucherAdminServiceImpl;
import vn.edu.hcmuaf.fit.ttltw.utils.SidebarUtil;

@WebServlet("/user/voucher-detail")
public class VoucherDetailServlet extends HttpServlet {
    private VoucherAdminService voucherService;

    @Override
    public void init() {
        voucherService = new VoucherAdminServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Lấy tất cả voucher còn hạn
        List<Voucher> listVoucher = voucherService.getActiveVouchers();
        req.setAttribute("listVoucher", listVoucher);
        // Set sidebar data
        req.setAttribute("activeMenu", "voucher");
        SidebarUtil.setSidebarData(req);

        req.getRequestDispatcher("/views/user/voucherDetail.jsp").forward(req, resp);

    }
}
