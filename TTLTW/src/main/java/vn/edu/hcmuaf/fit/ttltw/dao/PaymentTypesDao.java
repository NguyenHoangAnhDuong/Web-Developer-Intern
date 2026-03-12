package vn.edu.hcmuaf.fit.ttltw.dao;

import org.jdbi.v3.core.Jdbi;
import vn.edu.hcmuaf.fit.ttltw.config.DBConnect;
import vn.edu.hcmuaf.fit.ttltw.model.PaymentTypes;
import java.util.List;

public class PaymentTypesDao {
    private final Jdbi jdbi = DBConnect.getJdbi();

    public List<PaymentTypes> findAll() {
        return jdbi.withHandle(h -> h.createQuery("SELECT id, name FROM payment_types")
                .mapToBean(PaymentTypes.class)
                .list());
    }

    public PaymentTypes findById(int id) {
        return jdbi.withHandle(h -> h.createQuery("SELECT id, name FROM payment_types WHERE id = :id")
                .bind("id", id)
                .mapToBean(PaymentTypes.class)
                .findOne()
                .orElse(null));
    }
}
