package com.example.realtime_message_application.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

@Service
@Profile("test")
public class InMemoryRateLimitingService implements RateLimitingService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(30)
                .refillGreedy(30, Duration.ofMinutes(1))
                .initialTokens(30)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public Bucket createDefaultBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .initialTokens(5)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public boolean tryConsume(String key, long tokens) {
        return getBucket(key).tryConsume(tokens);
    }

    @Override
    public Bucket getBucket(String key) {
        return buckets.computeIfAbsent(key, k -> createBucket());
    }

    @Override
    public long getSecondsUntilRefill(String key) {
        Bucket bucket = getBucket(key);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            return 0L;
        }
        return Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds();
    }

    @Override
    public void resetBucket(String key) {
        buckets.remove(key);
    }

    @Override
    public Long getRemainingTokens(String key) {
        return getBucket(key).getAvailableTokens();
    }
}
