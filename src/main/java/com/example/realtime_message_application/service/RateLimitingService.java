package com.example.realtime_message_application.service;

import io.github.bucket4j.Bucket;

public interface RateLimitingService {

    public Bucket createDefaultBucket();

    public boolean tryConsume(String key, long tokens);

    public Bucket getBucket(String key);

    public long getSecondsUntilRefill(String key);

    public void resetBucket(String key);

    public Long getRemainingTokens(String key);
}
