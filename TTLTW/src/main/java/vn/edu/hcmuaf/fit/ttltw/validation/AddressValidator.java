package vn.edu.hcmuaf.fit.ttltw.validation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
public class AddressValidator {
    // Đầu số hợp lệ các nhà mạng Việt Nam
    private static final Set<String> VN_PHONE_PREFIXES = new HashSet<>(Arrays.asList(
            // Viettel
            "032","033","034","035","036","037","038","039",
            "086","096","097","098",
            // Mobifone
            "070","076","077","078","079",
            "089","090","093",
            // Vinaphone
            "081","082","083","084","085",
            "088","091","094",
            // Vietnamobile
            "052","056","058","092",
            // Gmobile
            "059","099",
            // Reddi
            "055"
    ));
    public static class ValidationResult {
        public final boolean ok;
        public final String message;
        public ValidationResult(boolean ok, String message) {
            this.ok = ok;
            this.message = message;
        }
    }
    public static ValidationResult validateName(String name) {
        if (name == null || name.trim().isEmpty())
            return new ValidationResult(false, "Họ tên không được để trống.");
        String trimmed = name.trim();
        if (trimmed.length() < 2)
            return new ValidationResult(false, "Họ tên phải có ít nhất 2 ký tự.");
        if (trimmed.length() > 100)
            return new ValidationResult(false, "Họ tên không được vượt quá 100 ký tự.");
        return new ValidationResult(true, null);
    }
    public static ValidationResult validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty())
            return new ValidationResult(false, "Số điện thoại không được để trống.");
        String trimmed = phone.trim();
        if (!trimmed.matches("^[0-9]{10}$"))
            return new ValidationResult(false, "Số điện thoại phải có đúng 10 chữ số.");
        String prefix = trimmed.substring(0, 3);
        if (!VN_PHONE_PREFIXES.contains(prefix))
            return new ValidationResult(false,
                    "Đầu số \"" + prefix + "\" không hợp lệ. Vui lòng nhập số điện thoại Việt Nam.");
        return new ValidationResult(true, null);
    }
    public static ValidationResult validateAddress(String address) {
        if (address == null || address.trim().isEmpty())
            return new ValidationResult(false, "Địa chỉ không được để trống.");
        String trimmed = address.trim();
        if (trimmed.length() < 5)
            return new ValidationResult(false, "Địa chỉ phải có ít nhất 5 ký tự.");
        if (trimmed.length() > 500)
            return new ValidationResult(false, "Địa chỉ không được vượt quá 500 ký tự.");
        return new ValidationResult(true, null);
    }
}