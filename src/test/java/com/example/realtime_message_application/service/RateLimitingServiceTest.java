package com.example.realtime_message_application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RateLimitingServiceTest {

    @Autowired
    private RateLimitingService rateLimitingService;

    private String newKey(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        String userKey = newKey("within-limit");

        for (int i = 0; i < 30; i++) {
            boolean allowed = rateLimitingService.tryConsume(userKey, 1);
            assertTrue(allowed, "Lần " + (i + 1) + " phải được phép");
        }
    }

    @Test
    void shouldBlockRequestWhenLimitExceeded() {
        String userKey = newKey("exceeded");

        for (int i = 0; i < 30; i++) {
            assertTrue(rateLimitingService.tryConsume(userKey, 1));
        }

        assertFalse(rateLimitingService.tryConsume(userKey, 1), "Lần 31 phải bị chặn");
    }

    @Test
    void shouldReturnCorrectRemainingTokens() {
        String userKey = newKey("remaining");

        rateLimitingService.tryConsume(userKey, 1);
        rateLimitingService.tryConsume(userKey, 1);
        rateLimitingService.tryConsume(userKey, 1);

        long remaining = rateLimitingService.getRemainingTokens(userKey);
        assertEquals(27, remaining, "Sau 3 lần consume thì còn 27 token");
    }

    @Test
    void shouldHandleMultipleUsersIndependently() {
        String userA = newKey("userA");
        String userB = newKey("userB");

        for (int i = 0; i < 30; i++) {
            assertTrue(rateLimitingService.tryConsume(userA, 1));
        }

        assertFalse(rateLimitingService.tryConsume(userA, 1), "User A phải bị chặn");
        assertTrue(rateLimitingService.tryConsume(userB, 1), "User B vẫn phải được phép");
    }

    @Test
    void shouldAllowConsumeMultipleTokensAtOnce() {
        String userKey = newKey("multi-consume");

        assertTrue(rateLimitingService.tryConsume(userKey, 5), "Consume 5 token phải được phép");
        assertEquals(25, rateLimitingService.getRemainingTokens(userKey));
    }

    @Test
    void shouldAllowConsumeExactlyRemainingTokens() {
        String userKey = newKey("exact-consume");

        assertTrue(rateLimitingService.tryConsume(userKey, 30), "Consume đúng 30 token phải được phép");
        assertEquals(0, rateLimitingService.getRemainingTokens(userKey));
        assertFalse(rateLimitingService.tryConsume(userKey, 1), "Sau khi hết token thì phải bị chặn");
    }

    @Test
    void shouldRejectConsumeMoreThanRemainingTokens() {
        String userKey = newKey("over-consume");

        assertTrue(rateLimitingService.tryConsume(userKey, 29));
        assertEquals(1, rateLimitingService.getRemainingTokens(userKey));

        assertFalse(rateLimitingService.tryConsume(userKey, 2), "Consume nhiều hơn số token còn lại phải fail");
        assertEquals(1, rateLimitingService.getRemainingTokens(userKey), "Token không được tự trừ khi consume fail");
    }
}