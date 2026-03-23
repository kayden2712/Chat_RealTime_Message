package com.example.realtime_message_application.dto.message;

import java.time.Instant;
import java.util.List;

import com.example.realtime_message_application.enums.MessageType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageResponse {
    private final Long messageId;
    private final Long conversationId;
    private final Long senderId;
    private final String senderName;
    private final String content;
    private final MessageType messageType;
    private final Long replayToMessageId;
    private final String replayToContent;
    private final Instant createdAt;
    private final Instant editedAt;
    private final String deletedBy;
    private final List<MessageResponse> repliesResponse;
    private final boolean pinned;
}
