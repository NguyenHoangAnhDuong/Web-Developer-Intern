package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.VoucherAdminDao;
import vn.edu.hcmuaf.fit.ttltw.dao.VoucherAdminDaoImpl;
import vn.edu.hcmuaf.fit.ttltw.model.Voucher;
import vn.edu.hcmuaf.fit.ttltw.validation.VoucherAdminValidator;

import java.util.List;

public class VoucherAdminServiceImpl implements VoucherAdminService {

    private final VoucherAdminDao dao;

    public VoucherAdminServiceImpl(VoucherAdminDaoImpl dao) {
        this.dao = dao;
    }

    @Override
    public List<Voucher> getAll(String keyword, Integer status, int page, int limit) {
        int offset = (page - 1) * limit;
        return dao.getAll(keyword, status, offset, limit);
    }

    @Override
    public int countAll(String keyword, Integer status) {
        return dao.countAll(keyword, status);
    }

    @Override
    public Voucher getById(int id) {
        return dao.getById(id);
    }

    @Override
    public void createVoucher(Voucher v) {
        VoucherAdminValidator.validate(v, dao, null);
        dao.insert(v);
    }

    @Override
    public void updateVoucher(Voucher v) {
        VoucherAdminValidator.validate(v, dao, v.getId());
        dao.update(v);
    }

    @Override
    public void deleteVoucher(int id) {
        dao.delete(id);
    }

    @Override
    public void toggleStatus(int id) {
        dao.toggleStatus(id);
    }

}
