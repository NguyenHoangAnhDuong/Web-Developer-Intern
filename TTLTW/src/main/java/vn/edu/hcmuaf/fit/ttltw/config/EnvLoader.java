package vn.edu.hcmuaf.fit.ttltw.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Đọc file .env tại project root (hoặc theo system property "env.file").
 * Ưu tiên: System property → OS env → .env file.
 */
public final class EnvLoader {

    private static final Map<String, String> CACHE = loadOnce();

    private EnvLoader() {}

    private static Map<String, String> loadOnce() {
        Map<String, String> map = new HashMap<>();
        Path path = resolveEnvPath();
        if (path == null || !Files.isRegularFile(path)) {
            return Collections.unmodifiableMap(map);
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                int eq = trimmed.indexOf('=');
                if (eq <= 0) continue;
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                            || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                map.put(key, value);
            }
        } catch (IOException e) {
            System.err.println("[EnvLoader] Cannot read " + path + ": " + e.getMessage());
        }
        return Collections.unmodifiableMap(map);
    }

    private static Path resolveEnvPath() {
        String override = System.getProperty("env.file");
        if (override != null && !override.isBlank()) {
            return Path.of(override);
        }
        Path cwd = Path.of(System.getProperty("user.dir"), ".env");
        if (Files.isRegularFile(cwd)) return cwd;
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null) {
            Path tomcat = Path.of(catalinaBase, ".env");
            if (Files.isRegularFile(tomcat)) return tomcat;
        }
        return cwd;
    }

    /** Lấy giá trị theo thứ tự: -D system property → OS env → .env file. */
    public static String get(String key) {
        String v = System.getProperty(key);
        if (v != null && !v.isEmpty()) return v;
        v = System.getenv(key);
        if (v != null && !v.isEmpty()) return v;
        return CACHE.get(key);
    }

    public static String get(String key, String defaultValue) {
        String v = get(key);
        return v != null ? v : defaultValue;
    }
}
