package vn.edu.hcmuaf.fit.ttltw.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.InputStream;
import java.util.Properties;

public class CloudinaryUtil {
    private static Cloudinary cloudinary;
    static {
        try {
            Properties props = new Properties();
            try (InputStream input = CloudinaryUtil.class
                    .getClassLoader()
                    .getResourceAsStream("application.properties")) {
                if (input == null) {
                    throw new RuntimeException("Không tìm thấy application.properties");
                }
                props.load(input);
            }
            String cloudName = props.getProperty("cloudinary.cloud_name");
            String apiKey = props.getProperty("cloudinary.api_key");
            String apiSecret = props.getProperty("cloudinary.api_secret");
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));
            System.out.println("Cloudinary initialized");
        } catch (Exception e) {
            throw new RuntimeException("Cloudinary init error", e);
        }
    }
    public static Cloudinary getInstance() {
        return cloudinary;
    }
}