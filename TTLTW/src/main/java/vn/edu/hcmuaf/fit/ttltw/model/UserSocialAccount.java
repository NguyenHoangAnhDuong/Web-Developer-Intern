package vn.edu.hcmuaf.fit.ttltw.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class UserSocialAccount implements Serializable {
    private int id;
    private int userId;
    private String provider;
    private String providerUserId;
    private String providerEmail;
    private Timestamp createdAt;

    public UserSocialAccount() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }

    public String getProviderEmail() { return providerEmail; }
    public void setProviderEmail(String providerEmail) { this.providerEmail = providerEmail; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "UserSocialAccount{" +
                "id=" + id +
                ", userId=" + userId +
                ", provider='" + provider + '\'' +
                ", providerUserId='" + providerUserId + '\'' +
                ", providerEmail='" + providerEmail + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
