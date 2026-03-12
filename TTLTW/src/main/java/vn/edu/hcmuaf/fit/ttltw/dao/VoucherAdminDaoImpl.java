package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Jdbi;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.Voucher;

import java.util.List;

public class VoucherAdminDaoImpl implements VoucherAdminDao {

    private final Jdbi jdbi = DBConnect.getJdbi();

    @Override
    public List<Voucher> getAll(String keyword, Integer status, int offset, int limit) {
        String sql = """
                    SELECT * FROM vouchers
                    WHERE (:keyword IS NULL OR voucher_code LIKE CONCAT('%', :keyword, '%'))
                    AND (:status IS NULL OR status = :status)
                    ORDER BY created_at DESC
                    LIMIT :limit OFFSET :offset
                """;

        return jdbi.withHandle(h -> h.createQuery(sql)
                .bind("keyword", keyword)
                .bind("status", status)
                .bind("limit", limit)
                .bind("offset", offset)
                .mapToBean(Voucher.class)
                .list());
    }

    @Override
    public int countAll(String keyword, Integer status) {
        String sql = """
                    SELECT COUNT(*) FROM vouchers
                    WHERE (:keyword IS NULL OR voucher_code LIKE CONCAT('%', :keyword, '%'))
                    AND (:status IS NULL OR status = :status)
                """;

        return jdbi.withHandle(h -> h.createQuery(sql)
                .bind("keyword", keyword)
                .bind("status", status)
                .mapTo(Integer.class)
                .one());
    }

    @Override
    public Voucher getById(int id) {
        return jdbi.withHandle(h -> h.createQuery("SELECT * FROM vouchers WHERE id = :id")
                .bind("id", id)
                .mapToBean(Voucher.class)
                .findOne()
                .orElse(null));
    }

    @Override
    public boolean isCodeExist(String voucherCode, Integer excludeId) {
        String sql = """
                    SELECT COUNT(*) FROM vouchers
                    WHERE voucher_code = :code
                    AND (:exclude IS NULL OR id != :exclude)
                """;
        int count = jdbi.withHandle(h -> h.createQuery(sql)
                .bind("code", voucherCode)
                .bind("exclude", excludeId)
                .mapTo(Integer.class)
                .one());
        return count > 0;
    }

    @Override
    public boolean insert(Voucher v) {
        String sql = """
                    INSERT INTO vouchers
                    (voucher_code, discount_amount, type, status, min_order_value, max_reduce, quantity,
                     start_date, end_date, created_at, updated_at)
                    VALUES (:voucherCode, :discountAmount, :type, :status, :minOrderValue, :maxReduce,
                            :quantity, :startDate, :endDate, NOW(), NOW())
                """;
        int rows = jdbi.withHandle(h -> h.createUpdate(sql)
                .bindBean(v)
                .execute());
        return rows > 0;
    }

    @Override
    public boolean update(Voucher v) {
        String sql = """
                    UPDATE vouchers SET
                    voucher_code = :voucherCode,
                    discount_amount = :discountAmount,
                    type = :type,
                    min_order_value = :minOrderValue,
                    max_reduce = :maxReduce,
                    quantity = :quantity,
                    start_date = :startDate,
                    end_date = :endDate,
                    updated_at = NOW()
                    WHERE id = :id
                """;
        int rows = jdbi.withHandle(h -> h.createUpdate(sql)
                .bindBean(v)
                .execute());

        return rows > 0;
    }

    @Override
    public boolean delete(int id) {
        return jdbi.withHandle(h -> h.createUpdate("DELETE FROM vouchers WHERE id = :id")
                .bind("id", id)
                .execute()) > 0;
    }

    @Override
    public boolean toggleStatus(int id) {
        String sql = """
                    UPDATE vouchers
                    SET status = CASE WHEN status = 1 THEN 0 ELSE 1 END
                    WHERE id = :id
                """;

        return jdbi.withHandle(h -> h.createUpdate(sql)
                .bind("id", id)
                .execute()) > 0;
    }

    @Override
    public List<Voucher> getActiveVouchers() {
        String sql = """
                    SELECT *
                    FROM vouchers
                    WHERE status = 1
                      AND start_date <= NOW()
                      AND end_date >= NOW()
                    ORDER BY end_date ASC
                """;
        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .mapToBean(Voucher.class)
                .list());
    }

    @Override
    public Voucher getByCode(String code) {
        String sql = "SELECT * FROM vouchers WHERE voucher_code = :code LIMIT 1";

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .bind("code", code)
                .mapToBean(Voucher.class)
                .findFirst()
                .orElse(null) // Nếu không thấy thì trả về null
        );
    }
}
