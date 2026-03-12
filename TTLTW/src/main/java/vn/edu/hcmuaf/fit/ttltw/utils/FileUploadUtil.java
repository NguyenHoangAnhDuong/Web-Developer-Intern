package vn.edu.hcmuaf.fit.ttltw.utils;

import jakarta.servlet.http.Part;

import java.io.File;
import java.nio.file.Paths;

public class FileUploadUtil {

    private static final String UPLOAD_DIR = "assert/img/product";

    public static String saveImage(Part part, String realPath) throws Exception {
        if (part == null || part.getSize() == 0) return null;

        String fileName = Paths.get(part.getSubmittedFileName())
                .getFileName()
                .toString();
        File dir = new File(realPath + File.separator + UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        part.write(dir.getAbsolutePath() + File.separator + fileName);
        return fileName;
    }
}
