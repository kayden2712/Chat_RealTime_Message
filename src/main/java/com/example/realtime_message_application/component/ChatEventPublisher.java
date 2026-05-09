package com.example.realtime_message_application.component;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.realtime_message_application.enums.ConversationType;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.service.ConversationService;

@Component
public class ChatEventPublisher {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ConversationService conversationService;

    public ChatEventPublisher(SimpMessagingTemplate simpMessagingTemplate, ConversationService conversationService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.conversationService = conversationService;
    }

    public void broadcastToConversation(Long conversationId, Long senderId, Object payload) {
        Conversation conversation = conversationService.getEntityByConvId(conversationId);

        if (conversation.getType() == ConversationType.GROUP) {
            simpMessagingTemplate.convertAndSend("/topic/conversations." + conversationId, payload);
        }
        else {
            Long receiverId = conversationService.getReceiverId(conversationId, senderId);
            simpMessagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/conversation." + conversationId, payload);
            simpMessagingTemplate.convertAndSendToUser(String.valueOf(senderId), "/queue/conversation." + conversationId, payload);
        }
    }
}
