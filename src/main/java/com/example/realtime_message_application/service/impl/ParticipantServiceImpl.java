package com.example.realtime_message_application.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.realtime_message_application.dto.conversation.ParticipantResponse;
import com.example.realtime_message_application.mapper.ConversationMapper;
import com.example.realtime_message_application.repository.ParticipantRepository;
import com.example.realtime_message_application.service.ParticipantService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantServiceImpl implements ParticipantService {
    private final ParticipantRepository particitantRepository;
    private final ConversationMapper conversationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantResponse> getAllParticipantsInConv(Long conversationId) {
        return particitantRepository.findAllParticipantsByConversationId(conversationId)
                .stream().map(conversationMapper::toParticipantResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getAdminsCountInConv(Long conversationId) {
        return particitantRepository.countNoOfAdminsInConv(conversationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getMembersCountInConv(Long conversationId) {
        return particitantRepository.countNoOfMembersInConv(conversationId);
    }

}
