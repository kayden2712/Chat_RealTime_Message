package com.example.realtime_message_application.service;

import java.util.List;

import com.example.realtime_message_application.dto.message.MessageResponse;

public interface NotificationService {

    void notifyNewMessage(Long conversationId, Long messageId, Long senderId, String senderName, String body);

    void notifyParticipants(Long conversationId, MessageResponse message);

    void sendBulkFirebaseNotification(List<String> tokens, String title, String body);
}
