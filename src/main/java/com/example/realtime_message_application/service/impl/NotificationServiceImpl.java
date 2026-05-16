package com.example.realtime_message_application.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.realtime_message_application.dto.message.MessageResponse;
import com.example.realtime_message_application.dto.notification.NotificationDTO;
import com.example.realtime_message_application.dto.notification.RedisNotificationEvent;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.model.ConversationParticipant;
import com.example.realtime_message_application.model.FCMToken;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.FCMTokenRepository;
import com.example.realtime_message_application.service.ConversationService;
import com.example.realtime_message_application.service.NotificationService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final ConversationService conversationService;
    private final FCMTokenRepository fcmrRepository;
    private final RedisTemplate<String, Object> redisPubSubTemplate;
    private final ChannelTopic notificationTopic;

    // WebSocket based notification
    // works only if the user is connected
    @Override
    public void notifyNewMessage(Long conversationId, Long messageId, Long senderId, String senderName, String body) {
        NotificationDTO notification = new NotificationDTO("NEW_MESSAGE", conversationId, messageId, senderName, body,
                Instant.now());

        Conversation conv = conversationService.getEntityByConvId(conversationId);

        List<Long> recipientIds = conv.getParticipants().stream()
                .map(p -> p.getUser().getUserId())
                .filter(id -> !id.equals(senderId))
                .toList();
        if(!recipientIds.isEmpty()){
            RedisNotificationEvent notificationEvent = new RedisNotificationEvent(recipientIds, notification);
            redisPubSubTemplate.convertAndSend(notificationTopic.getTopic(), notificationEvent);
            log.info("Published notification event to Redis for conversation: {}", conversationId);
        }
    }

    @Override
    public void notifyParticipants(Long conversationId, MessageResponse message) {

        Conversation coversation = conversationService.getEntityByConvId(conversationId);
        List<User> participants = new ArrayList<>(
                coversation.getParticipants().stream().map(ConversationParticipant::getUser)
                        .filter(u -> !u.getUserId().equals(message.getSenderId())).toList());
        List<String> tokens = fcmrRepository.findAllByUserIn(participants).stream().map(FCMToken::getToken).toList();
        String title = message.getSenderName();
        String body = message.getContent() != null
                ? message.getContent().substring(0, Math.min(40, message.getContent().length()))
                : "";

        if (!tokens.isEmpty()) {
            sendBulkFirebaseNotification(tokens, title, body);
        }

    }

    @Override
    public void sendBulkFirebaseNotification(List<String> tokens, String title, String body) {
        // Implement FCM notification sending logic here
        // This typically involves making an HTTP POST request to the FCM API with the
        // token, title, and body
        // You can use libraries like Firebase Admin SDK or make raw HTTP requests using
        // RestTemplate or WebClient
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        // Sử dụng MulticastMessage để gửi cho nhiều người cùng lúc
        MulticastMessage multicastMessage = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(notification)
                .putData("Type", "Message")
                .build();

        try {
            FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage);
        } catch (FirebaseMessagingException e) {
            log.error("Error sending bulk notifications: {}", e.getMessage());
        }
    }
}
