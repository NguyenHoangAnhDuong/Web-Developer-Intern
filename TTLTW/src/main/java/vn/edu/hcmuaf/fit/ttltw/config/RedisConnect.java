package vn.edu.hcmuaf.fit.ttltw.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPool;

import java.io.InputStream;
import java.util.Properties;

public class RedisConnect {

    private static JedisPool jedisPool;
    private static int ttlOtp;
    private static int ttlProduct;
    private static int ttlPendingUser;

    static {
        try {
            Properties props = new Properties();

            try (InputStream input = RedisConnect.class
                    .getClassLoader()
                    .getResourceAsStream("redis.properties")) {

                if (input == null) {
                    throw new RuntimeException("Không tìm thấy redis.properties");
                }

                props.load(input);
            }

            String host     = props.getProperty("redis.host", "localhost");
            int    port     = Integer.parseInt(props.getProperty("redis.port", "6379"));
            String password = props.getProperty("redis.password", "");
            int    timeout  = Integer.parseInt(props.getProperty("redis.timeout", "2000"));

            ttlOtp         = Integer.parseInt(props.getProperty("redis.ttl.otp",          "300"));
            ttlProduct     = Integer.parseInt(props.getProperty("redis.ttl.product",      "1800"));
            ttlPendingUser = Integer.parseInt(props.getProperty("redis.ttl.pending_user", "600"));

            GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
            poolConfig.setMaxTotal(Integer.parseInt(props.getProperty("redis.pool.maxTotal", "20")));
            poolConfig.setMaxIdle (Integer.parseInt(props.getProperty("redis.pool.maxIdle",  "5")));
            poolConfig.setMinIdle (Integer.parseInt(props.getProperty("redis.pool.minIdle",  "1")));
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);

            DefaultJedisClientConfig.Builder clientConfigBuilder = DefaultJedisClientConfig.builder()
                    .timeoutMillis(timeout);

            if (password != null && !password.isBlank()) {
                clientConfigBuilder.password(password);
            }

            JedisClientConfig clientConfig = clientConfigBuilder.build();

            jedisPool = new JedisPool(poolConfig, new HostAndPort(host, port), clientConfig);

            Runtime.getRuntime().addShutdownHook(new Thread(RedisConnect::shutdown));

            System.out.println("✔ Redis initialized");

        } catch (Exception e) {
            throw new RuntimeException("RedisConnect init error", e);
        }
    }

    public static Jedis getConnection() {
        return jedisPool.getResource();
    }

    public static int getTtlOtp() {
        return ttlOtp;
    }

    public static int getTtlProduct() {
        return ttlProduct;
    }

    public static int getTtlPendingUser() {
        return ttlPendingUser;
    }

    public static void shutdown() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
