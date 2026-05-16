package com.example.realtime_message_application.dto.notification;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisNotificationEvent {
    private List<Long> receiverIds;
    private NotificationDTO payload;
}
