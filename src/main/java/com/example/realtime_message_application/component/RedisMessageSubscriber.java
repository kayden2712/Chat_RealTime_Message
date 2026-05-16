package com.example.realtime_message_application.component;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.realtime_message_application.dto.message.RedisChatEvent;
import com.example.realtime_message_application.enums.ConversationType;
import com.example.realtime_message_application.service.ConversationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisMessageSubscriber {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConversationService conversationService;
    private final ObjectMapper objectMapper;

    // Hàm này được cấu hình tự động chạy khi Redis nhận được tin nhắn
    public void onMessage(Object message) {
        try {
            RedisChatEvent chatEvent = objectMapper.convertValue(message, RedisChatEvent.class);

            // Giải nén payload gốc
            Object originalPayload = objectMapper.readValue(chatEvent.getPayloadJson(), Object.class);

            Long conversationId = chatEvent.getConversationId();
            Long senderId = chatEvent.getSenderId();

            if(chatEvent.getConversationType() == ConversationType.GROUP){
                simpMessagingTemplate.convertAndSend("/topic/group/" + conversationId, originalPayload);
                log.info("Redis Sub: Broadcast group message to conversation {}", conversationId);
            }
            else{
                Long receiverId = conversationService.getReceiverId(conversationId, senderId);
                simpMessagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/conversation/" + conversationId, originalPayload);
                simpMessagingTemplate.convertAndSendToUser(String.valueOf(senderId), "/queue/conversation/" + conversationId, originalPayload);
                log.info("Redis Sub: Dispatched private message from {} to {}", senderId, receiverId);
            }

        } catch (Exception e) {
            log.error("Error processing Redis Message Pub/Sub", e);
        }
    }
}
