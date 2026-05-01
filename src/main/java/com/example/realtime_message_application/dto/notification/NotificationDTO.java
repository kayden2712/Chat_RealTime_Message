package com.example.realtime_message_application.dto.notification;

import java.time.Instant;

public record NotificationDTO(String type, Long conversationId, Long messageId, String title, String body, Instant timestamp) {

}
