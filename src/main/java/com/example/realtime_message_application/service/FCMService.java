package com.example.realtime_message_application.service;

import java.util.List;

public interface FCMService {
    void registerToken(Long userId, String token);

    List<String> getUserTokens(Long userId);

    void deleteToken(String token);
}
