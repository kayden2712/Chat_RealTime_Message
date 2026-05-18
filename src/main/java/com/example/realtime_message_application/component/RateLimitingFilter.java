package com.example.realtime_message_application.component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.realtime_message_application.service.RateLimitingService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, java.io.IOException {

        String userId = request.getHeader("x-User_Id");
        String key = userId == null || userId.isBlank() ? "IP: " + request.getRemoteAddr() : "userId: " + userId;

        boolean allowed = rateLimitingService.tryConsume(key, 1);

        log.info(
                "Remaining tokens: {}",
                rateLimitingService.getRemainingTokens(key)
        );

        if (!allowed) {
            long waitSec = rateLimitingService.getSecondsUntilRefill(key);
            log.warn("User {} is rate limited, wait {} seconds", userId, waitSec);

            String retryAt = DateTimeFormatter.RFC_1123_DATE_TIME
                    .format(ZonedDateTime.now().plusSeconds(waitSec));

            response.setStatus(429);
            response.setHeader("Retry-After", retryAt + " seconds");
            response.getWriter().write("Rate limit exceeded, wait " + waitSec + " seconds");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
