package vn.edu.hcmuaf.fit.ttltw.model;

import java.io.Serializable;

public class PaymentTypes implements Serializable{
    private int id;
    private String name;

    public PaymentTypes() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
