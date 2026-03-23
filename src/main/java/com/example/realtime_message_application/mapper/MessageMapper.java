package com.example.realtime_message_application.mapper;

import org.springframework.stereotype.Component;

import com.example.realtime_message_application.dto.message.MessageResponse;
import com.example.realtime_message_application.model.Message;

@Component
public class MessageMapper {

    public MessageResponse toMessageResponse(Message message) {
        return toMessageResponse(message, 1);
    }

    private MessageResponse toMessageResponse(Message message, int depth) {
        return MessageResponse.builder()
                .messageId(message.getMessageId())
                .conversationId(message.getConversation().getConversationId())
                .senderId(message.getSender().getUserId())
                .senderName(message.getSender().getNickname())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .replayToMessageId(message.getReplyTo() != null ? message.getReplyTo().getMessageId() : null)
                .replayToContent(message.getReplyTo() != null
                        ? (message.getReplyTo().isDeleted() ? "Đã xóa" : message.getReplyTo().getContent())
                        : null)
                .createdAt(message.getCreatedAt())
                .editedAt(message.getEditedAt())
                .repliesResponse(message.getReplies().isEmpty() ? null
                        : message.getReplies().stream().map(reply -> toMessageResponse(reply, depth + 1)).toList())
                .pinned(message.isPinned())
                .deletedBy(message.getDeletedBy())
                .build();
    }
}
