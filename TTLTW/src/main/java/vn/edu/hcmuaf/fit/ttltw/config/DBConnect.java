package vn.edu.hcmuaf.fit.ttltw.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;

import java.io.InputStream;
import java.util.Properties;

public class DBConnect {

    private static Jdbi jdbi;
    private static HikariDataSource dataSource;

    static {
        try {

            Properties props = new Properties();

            try (InputStream input = DBConnect.class
                    .getClassLoader()
                    .getResourceAsStream("db.properties")) {

                if (input == null) {
                    throw new RuntimeException("Không tìm thấy db.properties");
                }

                props.load(input);
            }

            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");
            int poolSize = Integer.parseInt(props.getProperty("db.poolsize", "10"));

            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);

            config.setMaximumPoolSize(poolSize);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            config.setConnectionTestQuery("SELECT 1");
            config.setLeakDetectionThreshold(60000);

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useUnicode", "true");
            config.addDataSourceProperty("characterEncoding", "utf8");

            dataSource = new HikariDataSource(config);
            jdbi = Jdbi.create(dataSource);

            Runtime.getRuntime().addShutdownHook(new Thread(DBConnect::shutdown));

            System.out.println("✔ DB initialized");

        } catch (Exception e) {
            throw new RuntimeException("DBConnect init error", e);
        }
    }

    public static Jdbi getJdbi() {
        return jdbi;
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}