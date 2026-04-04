package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.model.Voucher;
import java.util.List;

public interface VoucherAdminService {
    List<Voucher> getAll(String keyword, Integer status, int page, int limit);

    int countAll(String keyword, Integer status);

    Voucher getById(int id);

    void createVoucher(Voucher voucher);

    void updateVoucher(Voucher voucher);

    void deleteVoucher(int id);

    void toggleStatus(int id);
    List<Voucher> getActiveVouchers();

}
