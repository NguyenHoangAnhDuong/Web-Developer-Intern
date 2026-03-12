package vn.edu.hcmuaf.fit.ttltw.utils;

import org.mindrot.jbcrypt.BCrypt;

public class HashPasswordUtil {

    // Băm mật khẩu
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    // So sánh mật khẩu khi login
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
