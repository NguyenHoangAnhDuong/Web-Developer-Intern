package vn.edu.hcmuaf.fit.ttltw.controller.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.ttltw.model.BankAccount;
import vn.edu.hcmuaf.fit.ttltw.model.User;
import vn.edu.hcmuaf.fit.ttltw.service.BankAccountService;
import vn.edu.hcmuaf.fit.ttltw.utils.SidebarUtil;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "BankAccountServlet", urlPatterns = { "/user/payment" })
public class BankAccountServlet extends HttpServlet {

    private BankAccountService bankService;

    @Override
    public void init() {
        this.bankService = new BankAccountService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Optional<BankAccount> account = bankService.getByUserId(user.getId());
        if (account.isPresent()) {
            BankAccount ba = account.get();
            req.setAttribute("bankAccount", ba);
        }

        // Set sidebar data
        req.setAttribute("activeMenu", "bank");
        SidebarUtil.setSidebarData(req);

        req.getRequestDispatcher("/views/user/bankAccount.jsp").forward(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String bankName = req.getParameter("bank_name");
        String accountNumber = req.getParameter("account_number");
        String accountName = req.getParameter("account_name");

        Optional<BankAccount> existing = bankService.getByUserId(user.getId());
        boolean success;

        if (existing.isPresent()) {

            // UPDATE
            BankAccount b = existing.get();
            b.setBankName(bankName);
            b.setAccountNumber(accountNumber);
            b.setAccountName(accountName);
            success = bankService.update(b);
        } else {
            // INSERT
            BankAccount b = new BankAccount();
            b.setUserId(user.getId());
            b.setBankName(bankName);
            b.setAccountNumber(accountNumber);
            b.setAccountName(accountName);
            success = bankService.insert(b);
        }
        resp.sendRedirect(req.getContextPath() + "/user/payment");
    }

}
