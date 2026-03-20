package com.example.realtime_message_application.dto.conversation;

import java.time.Instant;
import java.util.List;

import com.example.realtime_message_application.enums.ConversationType;

public record ConversationResponse(
    Long ConversationId,
    String title,
    String description,
    String convCreatorName,
    ConversationType type,
    String avatarUrl,
    Instant createAt,
    List<ParticipantResponse> participants
    
) {
    public static ConversationResponse deleted(String response){
        return new ConversationResponse(null, response, null, null, null, null, null, null);
    }
}
