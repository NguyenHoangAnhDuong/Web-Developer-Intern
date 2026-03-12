package vn.edu.hcmuaf.fit.ttltw.model;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.io.Serializable;
import java.sql.Timestamp;

public class BankAccount implements Serializable {
    private int id;
    @ColumnName("user_id")
    private int userId;
    @ColumnName("bank_name")
    private String bankName;
    @ColumnName("account_number")
    private String accountNumber;
    @ColumnName("account_name")
    private String accountName;
    @ColumnName("linked_date")
    private Timestamp linkedDate;
    private int status;
    @ColumnName("updated_at")
    private Timestamp updatedAt;

    public BankAccount(int id, int userId, String bankName, String accountNumber, String accountName, Timestamp linkedDate, int status, Timestamp updatedAt) {
        this.id = id;
        this.userId = userId;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.linkedDate = linkedDate;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public BankAccount() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Timestamp getLinkedDate() {
        return linkedDate;
    }

    public void setLinkedDate(Timestamp linkedDate) {
        this.linkedDate = linkedDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
