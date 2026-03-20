package com.example.realtime_message_application.service;

import java.util.List;

import com.example.realtime_message_application.dto.conversation.ParticipantResponse;

public interface ParticipantService {
    
    List<ParticipantResponse> getAllParticipantsInConv(Long conversationId);

    Long getAdminsCountInConv(Long conversationId);

    Long getMembersCountInConv(Long conversationId);

}
