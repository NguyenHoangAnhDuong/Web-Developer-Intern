package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.ttltw.dao.VoucherAdminDaoImpl;
import vn.edu.hcmuaf.fit.ttltw.model.Voucher;
import vn.edu.hcmuaf.fit.ttltw.utils.SidebarUtil;

import java.io.IOException;
import java.util.List;

@WebServlet("/user/voucher-detail")
public class VoucherDetailServlet extends HttpServlet {
    private VoucherAdminDaoImpl dao;

    @Override
    public void init() {
        dao = new VoucherAdminDaoImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Lấy tất cả voucher còn hạn
        List<Voucher> listVoucher = dao.getActiveVouchers();
        req.setAttribute("listVoucher", listVoucher);

        // Set sidebar data
        req.setAttribute("activeMenu", "voucher");
        SidebarUtil.setSidebarData(req);

        req.getRequestDispatcher("/views/user/voucherDetail.jsp").forward(req, resp);

    }
}
