package com.example.realtime_message_application.component;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.realtime_message_application.dto.notification.RedisNotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisNotificationSubscriber {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ObjectMapper objectMapper;

    // Hàm này được cấu hình tự động chạy khi Redis nhận được tin nhắn
    public void onNotification(Object message) {
        try {
            RedisNotificationEvent notificationEvent = objectMapper.convertValue(message, RedisNotificationEvent.class);

            for(Long receiverId : notificationEvent.getReceiverIds()){
                simpMessagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/notifications", notificationEvent.getPayload());
                log.info("Redis Sub: Dispatched notification to user {}", receiverId);
            }

        } catch (Exception e) {
            log.error("Error processing Redis Notification Pub/Sub", e);
        }
    }
}
