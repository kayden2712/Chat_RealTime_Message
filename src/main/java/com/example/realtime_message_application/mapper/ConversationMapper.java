package com.example.realtime_message_application.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.realtime_message_application.dto.conversation.ConversationDTO;
import com.example.realtime_message_application.dto.conversation.ConversationResponse;
import com.example.realtime_message_application.dto.conversation.ParticipantResponse;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.model.ConversationParticipant;

@Component
public class ConversationMapper {
    public ParticipantResponse toParticipantResponse(ConversationParticipant participant) {
        return new ParticipantResponse(
                participant.getCpId(),
                participant.getUser().getUserId(),
                participant.getConversation().getConversationId(),
                participant.getUser().getNickname(),
                participant.getParticipantRole().name(),
                participant.getJoinedOn());
    };

    public ConversationResponse toConversationResponse(Conversation conversation, List<ConversationParticipant> participants){
        return new ConversationResponse(
            conversation.getConversationId(),
            conversation.getTitle(),
            conversation.getDescription(),
            conversation.getCreator().getNickname(),
            conversation.getType(),
            conversation.getAvatarUrl() == null ? "No image" : conversation.getAvatarUrl(),
            conversation.getCreatedAt(),
            participants.stream().map(this::toParticipantResponse).toList()
        );
    }

    public ConversationResponse deletedConversationResponse(String message){
        return ConversationResponse.deleted(message);
    }
}
