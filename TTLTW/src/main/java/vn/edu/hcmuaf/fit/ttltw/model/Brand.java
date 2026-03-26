package vn.edu.hcmuaf.fit.ttltw.model;

import java.io.Serializable;

public class Brand implements Serializable {
    private Integer id;
    private String name;        // Samsung, iPhone, Oppo
    private String img;

    public Brand() {
    }
    public Brand(Integer id) {
        this.id = id;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
