package com.example.realtime_message_application.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.realtime_message_application.model.FCMToken;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.FCMTokenRepository;
import com.example.realtime_message_application.service.FCMService;
import com.example.realtime_message_application.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMServiceImpl implements FCMService {

    private final UserService userService;
    private final FCMTokenRepository fcmTokenRepository;

    @Override
    public void registerToken(Long userId, String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token must not be null or blank");
        }
        User user = userService.getEntityByUserId(userId);
        fcmTokenRepository.findByUser(user).ifPresentOrElse(existing -> {
            log.info("Token already exists, skipping registration.");
        }, () -> {
            FCMToken newToken = FCMToken.builder()
                    .user(user)
                    .token(token)
                    .build();
            fcmTokenRepository.save(newToken);
            log.info("Registered FCM token for user {}: {}", userId, token);
        });
    }

    @Override
    public List<String> getUserTokens(Long userId) {
        return fcmTokenRepository.findByUser_UserId(userId)
                .stream()
                .map(FCMToken::getToken)
                .toList();
    }

    @Override
    public void deleteToken(String token) {
        fcmTokenRepository.deleteByToken(token);
        log.info("Deleted FCM token: {}", token);
    }

}
