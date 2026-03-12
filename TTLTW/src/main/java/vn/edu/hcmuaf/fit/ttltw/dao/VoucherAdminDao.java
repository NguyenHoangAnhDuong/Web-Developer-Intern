package vn.edu.hcmuaf.fit.ttltw.dao;

import vn.edu.hcmuaf.fit.ttltw.model.Voucher;

import java.util.List;

public interface VoucherAdminDao {
    // Lấy danh sách voucher với bộ lọc và phân trang
    List<Voucher> getAll(String keyword, Integer status, int offset, int limit);

    // Đếm tổng số voucher với bộ lọc
    int countAll(String keyword, Integer status);

    // Lấy voucher theo ID
    Voucher getById(int id);

    // Kiểm tra mã voucher đã tồn tại (ngoại trừ ID được chỉ định)
    boolean isCodeExist(String voucherCode, Integer excludeId);

    // Thêm voucher mới
    boolean insert(Voucher voucher);

    // Cập nhật thông tin voucher
    boolean update(Voucher voucher);

    // Xóa voucher theo ID
    boolean delete(int id);

    // Chuyển đổi trạng thái hoạt động của voucher
    boolean toggleStatus(int id);

    // Lấy danh sách voucher đang hoạt động
    List<Voucher> getActiveVouchers();

    // Lấy voucher theo mã
    Voucher getByCode(String code);
}
