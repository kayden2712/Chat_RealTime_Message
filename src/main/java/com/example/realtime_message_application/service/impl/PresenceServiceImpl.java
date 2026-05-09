package com.example.realtime_message_application.service.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.service.ConversationService;
import com.example.realtime_message_application.service.PresenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceServiceImpl implements PresenceService {

    private final RedisTemplate<String, String> redisTemplate;

    private final ConversationService conversationService;

    // Quản lý hằng số để dễ bảo trì
    private final String USER_KEY = "user:online:";

    private static final Duration ONLINE_TIMEOUT = Duration.ofSeconds(30);

    @Override
    public boolean isOnline(Long userId) {
        String key = USER_KEY + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public Map<Long, String> getFriendsStatus(List<Long> friendIds) {
        Map<Long, String> statusMap = new HashMap<>();
        for (Long id : friendIds) {
            statusMap.put(id, isOnline(id) ? "ONLINE" : "OFFLINE");
        }
        return statusMap;
    }

    @Override
    public List<Long> getOnlineUserByConvId(Long convId) {

        Conversation conv = conversationService.getEntityByConvId(convId);

        if (conv == null) {
            log.warn("Conversation with id {} not found", convId);
            return List.of();
        }

        return conv.getParticipants().stream()
                .map(p -> p.getUser().getUserId())
                .filter(this::isOnline)
                .toList();
    }

    @Override
    public void markOnline(Long userId) {
        String key = USER_KEY + userId;
        redisTemplate.opsForValue().set(key, "ONLINE", ONLINE_TIMEOUT); // Set online status with expiration
        log.info("User {} marked as online", userId);
    }

    @Override
    public void markOffline(Long userId) {
        String key = USER_KEY + userId;
        redisTemplate.delete(key);
        log.info("User {} marked as offline", userId);
    }

    @Override
    public void refreshOnline(Long userId) {
        String key = USER_KEY + userId;
        redisTemplate.expire(key, ONLINE_TIMEOUT); // Refresh expiration time
        log.info("User {} online status refreshed", userId);
    }
}
