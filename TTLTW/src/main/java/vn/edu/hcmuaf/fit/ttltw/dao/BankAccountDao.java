package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Jdbi;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.BankAccount;

import java.util.Optional;

public class BankAccountDao {
    private final Jdbi jdbi; // Kết nối database

    public BankAccountDao() {
        this.jdbi = DBConnect.getJdbi();
    }

    // Tìm tài khoản ngân hàng theo user ID
    public Optional<BankAccount> findByUserId(int userId) {
        return jdbi
                .withHandle(handle -> handle.createQuery("SELECT * FROM bank_accounts WHERE user_id = :userId LIMIT 1")
                        .bind("userId", userId)
                        .mapToBean(BankAccount.class)
                        .findOne());
    }

    // Thêm tài khoản ngân hàng mới
    public boolean insert(BankAccount b) {
        int rows = jdbi.withHandle(handle -> handle
                .createUpdate(
                        """
                                    INSERT INTO bank_accounts (user_id, bank_name, account_number, account_name, linked_date, status, updated_at)
                                    VALUES (:userId, :bankName, :accountNumber, :accountName, NOW(), 1, NOW())
                                """)
                .bind("userId", b.getUserId())
                .bind("bankName", b.getBankName())
                .bind("accountNumber", b.getAccountNumber())
                .bind("accountName", b.getAccountName())
                .execute());
        return rows > 0;
    }

    // Cập nhật thông tin tài khoản ngân hàng
    public boolean update(BankAccount b) {
        return jdbi.withHandle(handle -> {

            int rows = handle.createUpdate("""
                        UPDATE bank_accounts
                        SET bank_name = :bankName,
                            account_number = :accountNumber,
                            account_name = :accountName,
                            updated_at = NOW()
                        WHERE user_id = :userId
                    """)
                    .bind("bankName", b.getBankName())
                    .bind("accountNumber", b.getAccountNumber())
                    .bind("accountName", b.getAccountName())
                    .bind("userId", b.getUserId())
                    .execute();

            return rows > 0;
        });
    }

}
