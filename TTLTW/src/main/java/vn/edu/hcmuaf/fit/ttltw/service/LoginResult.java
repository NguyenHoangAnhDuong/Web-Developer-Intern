package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.model.User;

// Kết quả của một lần đăng nhập, dùng chung cho login thường và login social.
// Service trả về object này để controller quyết định message hiển thị (sai mật khẩu, bị khóa, ...).
public class LoginResult {
    public enum Status {
        SUCCESS,
        INVALID_CREDENTIALS,   // sai user/mật khẩu (login thường)
        ACCOUNT_INACTIVE,      // status != 1 (admin disable vĩnh viễn)
        ACCOUNT_LOCKED,        // locked_until > NOW()
        SOCIAL_LOGIN_FAILED    // lỗi khác từ provider (không lấy được email, role không hợp lệ, ...)
    }

    private final Status status;
    private final User user;
    private final int remainingAttempts;   // chỉ có nghĩa khi INVALID_CREDENTIALS
    private final long lockSecondsRemaining; // chỉ có nghĩa khi ACCOUNT_LOCKED

    private LoginResult(Status status, User user, int remainingAttempts, long lockSecondsRemaining) {
        this.status = status;
        this.user = user;
        this.remainingAttempts = remainingAttempts;
        this.lockSecondsRemaining = lockSecondsRemaining;
    }

    public static LoginResult success(User user) {
        return new LoginResult(Status.SUCCESS, user, 0, 0);
    }

    public static LoginResult invalidCredentials(int remainingAttempts) {
        return new LoginResult(Status.INVALID_CREDENTIALS, null, Math.max(remainingAttempts, 0), 0);
    }

    public static LoginResult accountInactive() {
        return new LoginResult(Status.ACCOUNT_INACTIVE, null, 0, 0);
    }

    public static LoginResult accountLocked(long lockSecondsRemaining) {
        return new LoginResult(Status.ACCOUNT_LOCKED, null, 0, Math.max(lockSecondsRemaining, 0));
    }

    public static LoginResult socialLoginFailed() {
        return new LoginResult(Status.SOCIAL_LOGIN_FAILED, null, 0, 0);
    }

    public Status getStatus() { return status; }
    public User getUser() { return user; }
    public int getRemainingAttempts() { return remainingAttempts; }
    public long getLockSecondsRemaining() { return lockSecondsRemaining; }
}
