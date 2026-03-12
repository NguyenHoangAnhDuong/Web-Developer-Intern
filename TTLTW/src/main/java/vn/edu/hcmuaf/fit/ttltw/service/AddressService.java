package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.AddressDao;
import vn.edu.hcmuaf.fit.ttltw.model.Address;

import java.util.List;
import java.util.Optional;

public class AddressService {
    private final AddressDao addressDao;

    public AddressService() {
        this.addressDao = new AddressDao();
    }

    // Lấy địa chỉ theo ID
    public List<Address> getAll(int userId) {
        return addressDao.findAllByUserId(userId);
    }

    public int add(Address a) {
        if (addressDao.findAllByUserId(a.getUserId()).isEmpty()) {
            a.setStatus(1);
        }
        return addressDao.insert(a);
    }

    public boolean update(Address a) {
        Optional<Address> old = addressDao.findById(a.getId());
        return old.isPresent() && old.get().getUserId() == a.getUserId()
                && addressDao.update(a);
    }

    public boolean delete(int id, int userId) {
        Optional<Address> addr = addressDao.findById(id);
        if (addr.isEmpty() || addr.get().getUserId() != userId)
            return false;
        return addressDao.delete(id);
    }

    public boolean setDefault(int id, int userId) {
        return addressDao.findById(id)
                .filter(a -> a.getUserId() == userId)
                .map(a -> addressDao.setDefault(id, userId))
                .orElse(false);
    }

}
