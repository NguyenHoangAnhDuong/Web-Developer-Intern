package vn.edu.hcmuaf.fit.ttltw.validation;

public final class ProductPriceValidator {

    private ProductPriceValidator() {
    }

    public static class ValidationResult {
        public final boolean ok;
        public final String message;

        public ValidationResult(boolean ok, String message) {
            this.ok = ok;
            this.message = message;
        }
    }

    public static ValidationResult validatePositivePrice(String rawValue, String fieldLabel) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return new ValidationResult(false, fieldLabel + " không được để trống.");
        }

        double value;
        try {
            value = Double.parseDouble(rawValue.trim());
        } catch (NumberFormatException e) {
            return new ValidationResult(false, fieldLabel + " phải là số hợp lệ.");
        }

        if (value <= 0) {
            return new ValidationResult(false, fieldLabel + " phải lớn hơn 0.");
        }

        return new ValidationResult(true, null);
    }

    public static ValidationResult validatePositivePriceArray(String[] rawValues, String fieldLabel) {
        if (rawValues == null || rawValues.length == 0) {
            return new ValidationResult(false, fieldLabel + " không được để trống.");
        }

        for (int i = 0; i < rawValues.length; i++) {
            ValidationResult result = validatePositivePrice(rawValues[i], fieldLabel + " #" + (i + 1));
            if (!result.ok) {
                return result;
            }
        }

        return new ValidationResult(true, null);
    }
}