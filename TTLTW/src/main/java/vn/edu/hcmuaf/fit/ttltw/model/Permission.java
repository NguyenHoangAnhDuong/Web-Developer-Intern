package vn.edu.hcmuaf.fit.ttltw.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Permission implements Serializable {
    private int id;
    private String name;
    private String displayName;
    private String module;
    private Timestamp createdAt;

    public Permission() {}

    public Permission(String name, String displayName, String module) {
        this.name = name;
        this.displayName = displayName;
        this.module = module;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", module='" + module + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
