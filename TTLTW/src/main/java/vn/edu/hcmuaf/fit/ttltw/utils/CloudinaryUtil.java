package vn.edu.hcmuaf.fit.ttltw.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.http.Part;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Paths;
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

    public static String uploadImage(Part part, String folder) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }

        String contentType = part.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IOException("File tải lên phải là ảnh");
        }

        String submittedFileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
        String baseName = submittedFileName;
        int dotIndex = submittedFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = submittedFileName.substring(0, dotIndex);
        }

        String publicId = baseName.replaceAll("[^a-zA-Z0-9_-]", "_") + "_" + System.currentTimeMillis();
        byte[] fileBytes = part.getInputStream().readAllBytes();

        var upload = cloudinary.uploader().upload(
                fileBytes,
                ObjectUtils.asMap(
                        "folder", folder,
                        "public_id", publicId,
                        "overwrite", true,
                        "resource_type", "image"
                )
        );

        return upload.get("secure_url").toString();
    }
}