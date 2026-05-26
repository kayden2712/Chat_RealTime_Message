package com.example.realtime_message_application.service.impl;

import java.time.Duration;
import java.util.function.Supplier;

import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
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
    private final ProxyManager<String> proxyManager;
    private final RedissonClient redissonClient;

    public RateLimitingServiceImpl() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        this.redissonClient = Redisson.create(config);
        this.proxyManager = Bucket4jRedisson.casBasedBuilder(((Redisson) redissonClient).getCommandExecutor()).build();

        Bandwidth limit = Bandwidth.builder()
                .capacity(3) // Thùng chứa tối đa 5 Token
                .refillGreedy(3, Duration.ofMinutes(1)) // Mỗi phút nạp lại 5 Token
                .initialTokens(3) // Khởi tạo với 5 Token
                .build();

        this.bucketConfig = () -> BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    public Bucket createDefaultBucket() {
        Bandwidth limit = Bandwidth.builder().capacity(5).refillGreedy(5, Duration.ofMinutes(1)).initialTokens(5)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public boolean tryConsume(String key, long tokens) {

        // RAtomicLong là một kiểu dữ liệu của Redisson dùng để lưu trữ số nguyên
        // Nó được sử dụng để đếm số lần truy cập của người dùng
        RAtomicLong total = redissonClient.getAtomicLong("User:" + key + ": Total ");
        RAtomicLong blocked = redissonClient.getAtomicLong("User:" + key + ": Blocked ");
        total.incrementAndGet();

        log.info("Total access: " + total + "\nBlocked access: " + blocked);

        boolean allowed = getBucket(key).tryConsume(tokens);
        if (!allowed) {
            blocked.incrementAndGet();
            log.warn("User {} is rate limited", key);
        }
        log.info("Total access: " + total + "\nBlocked access: " + blocked);
        return allowed;
    }

    @Override
    public Bucket getBucket(String key) {
        return proxyManager.builder().build(key, bucketConfig);
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
        this.proxyManager.removeProxy(key);
    }

    @Override
    public Long getRemainingTokens(String key) {
        return getBucket(key).getAvailableTokens();
    }

}
