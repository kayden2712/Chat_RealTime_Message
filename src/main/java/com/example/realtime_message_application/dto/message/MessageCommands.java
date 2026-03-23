package com.example.realtime_message_application.dto.message;

import org.springframework.web.multipart.MultipartFile;

import com.example.realtime_message_application.enums.MessageType;

public final class MessageCommands {

    private MessageCommands() {
    }

    public record SendMessageCommand(
            Long conversationId,
            Long senderId,
            String content,
            MessageType messageType,
            MultipartFile file,
            Long replyToMessageId
    ) {
    }

    public record EditMessageCommand(Long messageId, Long conversationId, Long senderId, String newContent) {
    }

    public record DeleteMessageCommand(Long messageId, Long conversationId, Long actorId) {
    }

    public record PinMessageCommand(Long messageId, Long conversationId, Long actorId) {
    }
}
