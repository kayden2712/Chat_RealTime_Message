package com.example.realtime_message_application.component;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.realtime_message_application.dto.message.RedisChatEvent;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.service.ConversationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChatEventPublisher {

    private final ConversationService conversationService;
    private final ObjectMapper objectMapper;
    private final ChannelTopic chatEventTopic;
    private final RedisTemplate<String, Object> redisPubSubTemplate;

    public ChatEventPublisher(SimpMessagingTemplate simpMessagingTemplate, ConversationService conversationService,
            ObjectMapper objectMapper, ChannelTopic chatEventTopic, RedisTemplate<String, Object> redisPubSubTemplate) {
        this.conversationService = conversationService;
        this.objectMapper = objectMapper;
        this.chatEventTopic = chatEventTopic;
        this.redisPubSubTemplate = redisPubSubTemplate;
    }

    public void broadcastToConversation(Long conversationId, Long senderId, Object payload) {
        Conversation conversation = conversationService.getEntityByConvId(conversationId);

        try {
            // Chuyển payload thực tế (MessageEntity/DTO) thành chuỗi JSON
            String payloadJson = objectMapper.writeValueAsString(payload);

            // Đóng gói thành Event chuyển tiếp qua Redis
            RedisChatEvent chatEvent = new RedisChatEvent(conversationId, senderId, conversation.getType(),
                    payloadJson);

            // Gửi sự kiện đến Redis Pub/Sub
            redisPubSubTemplate.convertAndSend(chatEventTopic.getTopic(), chatEvent);
            log.info("Publish message to Redis Topic for conversation: {}", conversationId);
        } catch (Exception e) {
            log.error("Error occurred while publishing chat event", e);
        }
    }
}
