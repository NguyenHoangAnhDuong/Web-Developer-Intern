package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.AddressDao;
import vn.edu.hcmuaf.fit.ttltw.dao.PaymentTypesDao;
import vn.edu.hcmuaf.fit.ttltw.model.Address;
import vn.edu.hcmuaf.fit.ttltw.model.PaymentTypes;

import java.util.List;

public class PaymentTypesService {
    public final AddressDao addressDao = new AddressDao();
    private final PaymentTypesDao paymentTypeDao = new PaymentTypesDao();

    public List<Address> getUserAddresses(int userId) {
        return addressDao.findAllByUserId(userId);
    }

    public List<PaymentTypes> getPaymentTypes() {
        return paymentTypeDao.findAll();
    }
}
