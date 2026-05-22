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

public final class EnvLoader {

    private static final Map<String, String> CACHE = loadOnce();

    private EnvLoader() {}

    private static Map<String, String> loadOnce() {
        Map<String, String> map = new HashMap<>();
        Path path = resolveEnvPath();
        if (path == null || !Files.isRegularFile(path)) {
            System.err.println("[EnvLoader] No .env found. Looked from user.dir="
                    + System.getProperty("user.dir")
                    + " (walking up), catalina.base, catalina.home, and classpath.");
            return Collections.unmodifiableMap(map);
        }
        System.out.println("[EnvLoader] Loading env from " + path.toAbsolutePath());
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
        // 1) user.dir hiện tại
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path direct = cwd.resolve(".env");
        if (Files.isRegularFile(direct)) return direct;

        // 2) Đi ngược lên cây thư mục từ user.dir (Tomcat có thể start từ bin/ hoặc workdir lệch)
        Path dir = cwd;
        for (int i = 0; i < 6 && dir != null; i++) {
            Path candidate = dir.resolve(".env");
            if (Files.isRegularFile(candidate)) return candidate;
            dir = dir.getParent();
        }

        // 3) catalina.base / catalina.home
        for (String prop : new String[] { "catalina.base", "catalina.home" }) {
            String base = System.getProperty(prop);
            if (base != null) {
                Path p = Path.of(base, ".env");
                if (Files.isRegularFile(p)) return p;
            }
        }

        // 4) Classpath (WEB-INF/classes/.env) — fallback nếu user đặt .env vào resources
        try {
            java.net.URL res = EnvLoader.class.getClassLoader().getResource(".env");
            if (res != null) {
                return Path.of(res.toURI());
            }
        } catch (Exception ignored) {
        }

        return direct; // không tồn tại, để loadOnce() bỏ qua
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
