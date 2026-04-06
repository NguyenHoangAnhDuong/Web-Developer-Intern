package vn.edu.hcmuaf.fit.ttltw.model;

public class LoginResult {

    public enum Status {
        SUCCESS,
        WRONG_CREDENTIALS,
        ACCOUNT_LOCKED,
        JUST_LOCKED
    }

    private final Status status;
    private final User user;

    private LoginResult(Status status, User user) {
        this.status = status;
        this.user = user;
    }

    public Status getStatus() { return status; }
    public User getUser()     { return user; }

    public static LoginResult success(User user)          { return new LoginResult(Status.SUCCESS, user); }
    public static LoginResult wrongCredentials()          { return new LoginResult(Status.WRONG_CREDENTIALS, null); }
    public static LoginResult accountLocked(User user)    { return new LoginResult(Status.ACCOUNT_LOCKED, user); }
    public static LoginResult justLocked(User user)       { return new LoginResult(Status.JUST_LOCKED, user); }
}
