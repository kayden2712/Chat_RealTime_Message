package com.example.realtime_message_application.service.impl;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.example.realtime_message_application.service.RateLimitingService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.redisson.Bucket4jRedisson;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Profile("!test")
public class RateLimitingServiceImpl implements RateLimitingService {

    private final Supplier<BucketConfiguration> bucketConfig;
    private ProxyManager<String> proxyManager;
    private RedissonClient redissonClient;
    private volatile boolean redisAvailable = false;

    // Fallback in-memory khi Redis không kết nối được
    private final Map<String, Bucket> inMemoryBuckets = new ConcurrentHashMap<>();

    public RateLimitingServiceImpl(
            @Value("${spring.data.redis.url:}") String redisUrl,
            @Value("${spring.data.redis.host:127.0.0.1}") String redisHost,
            @Value("${spring.data.redis.port:6379}") int redisPort) {

        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .initialTokens(5)
                .build();

        this.bucketConfig = () -> BucketConfiguration.builder()
                .addLimit(limit)
                .build();

        try {
            Config config = new Config();
            SingleServerConfig serverConfig = config.useSingleServer();

            // Fail nhanh thay vì treo cả phút chờ Redis
            serverConfig.setConnectTimeout(3000);
            serverConfig.setTimeout(3000);
            serverConfig.setRetryAttempts(1);

            if (redisUrl != null && !redisUrl.isBlank()) {
                String cleanedUrl = redisUrl.trim();
                if (cleanedUrl.contains("redis://") || cleanedUrl.contains("rediss://")) {
                    cleanedUrl = cleanedUrl.substring(cleanedUrl.indexOf("redis"));
                }

                URI uri = URI.create(cleanedUrl);
                boolean isSsl = "rediss".equalsIgnoreCase(uri.getScheme());
                String host = uri.getHost();
                int port = uri.getPort() != -1 ? uri.getPort() : 6379;

                String scheme = isSsl ? "rediss://" : "redis://";
                serverConfig.setAddress(scheme + host + ":" + port);

                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":");
                    String password = userInfo.length > 1 ? userInfo[1] : userInfo[0];
                    serverConfig.setPassword(password);
                }

                if (isSsl) {
                    serverConfig.setSslEnableEndpointIdentification(false);
                }

                log.info("Connecting to Redis at {}:{} (SSL: {})", host, port, isSsl);
            } else {
                serverConfig.setAddress("redis://" + redisHost + ":" + redisPort);
                log.info("Connecting to Redis at {}:{}", redisHost, redisPort);
            }

            this.redissonClient = Redisson.create(config);

            CommandAsyncExecutor commandExecutor = ((Redisson) redissonClient).getCommandExecutor();
            this.proxyManager = Bucket4jRedisson.casBasedBuilder(commandExecutor).build();

            redisAvailable = true;
            log.info("✅ Redis connection established successfully!");

        } catch (Exception e) {
            redisAvailable = false;
            this.redissonClient = null;
            this.proxyManager = null;
            log.warn("⚠️ Could not connect to Redis ({}). Falling back to in-memory rate limiting.",
                    e.getMessage());
        }
    }

    @Override
    public Bucket createDefaultBucket() {
        Bandwidth limit = Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).initialTokens(5)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public boolean tryConsume(String key, long tokens) {
        if (redisAvailable) {
            try {
                RAtomicLong total = redissonClient.getAtomicLong("User:" + key + ": Total ");
                RAtomicLong blocked = redissonClient.getAtomicLong("User:" + key + ": Blocked ");
                total.incrementAndGet();

                boolean allowed = getBucket(key).tryConsume(tokens);
                if (!allowed) {
                    blocked.incrementAndGet();
                    log.warn("User {} is rate limited", key);
                }
                return allowed;
            } catch (Exception e) {
                log.warn("Redis call failed at runtime, switching to in-memory fallback: {}", e.getMessage());
                redisAvailable = false;
            }
        }
        return getBucket(key).tryConsume(tokens);
    }

    @Override
    public Bucket getBucket(String key) {
        if (redisAvailable) {
            try {
                return proxyManager.builder().build(key, bucketConfig);
            } catch (Exception e) {
                log.warn("Redis getBucket failed, using in-memory fallback: {}", e.getMessage());
                redisAvailable = false;
            }
        }
        return inMemoryBuckets.computeIfAbsent(key, k -> createDefaultBucket());
    }

    @Override
    public long getSecondsUntilRefill(String key) {
        Bucket bucket = getBucket(key);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed())
            return 0L;

        return Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds();
    }

    @Override
    public void resetBucket(String key) {
        if (redisAvailable) {
            try {
                this.proxyManager.removeProxy(key);
                return;
            } catch (Exception e) {
                log.warn("Redis resetBucket failed, using in-memory fallback: {}", e.getMessage());
                redisAvailable = false;
            }
        }
        inMemoryBuckets.remove(key);
    }

    @Override
    public Long getRemainingTokens(String key) {
        return getBucket(key).getAvailableTokens();
    }
}