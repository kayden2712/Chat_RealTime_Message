package com.example.realtime_message_application.service.impl;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.realtime_message_application.dto.conversation.ParticipantResponse;
import com.example.realtime_message_application.mapper.ConversationMapper;
import com.example.realtime_message_application.model.ConversationParticipant;
import com.example.realtime_message_application.repository.ParticipantRepository;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceImplTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private ConversationMapper conversationMapper;

    @InjectMocks
    private ParticipantServiceImpl participantService;

    private ConversationParticipant participant;
    private ParticipantResponse participantResponse;

    @BeforeEach
    void setUp() {
        participant = ConversationParticipant.builder().build();
        participantResponse = new ParticipantResponse(1L, 2L, 3L, "testuser", "MEMBER",
                java.time.Instant.now());
    }

    @Test
    void getAllParticipantsInConv_ShouldReturnList() {
        when(participantRepository.findAllParticipantsByConversationId(1L))
                .thenReturn(Set.of(participant));
        when(conversationMapper.toParticipantResponse(participant)).thenReturn(participantResponse);

        List<ParticipantResponse> result = participantService.getAllParticipantsInConv(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(participantRepository).findAllParticipantsByConversationId(1L);
    }

    @Test
    void getAdminsCountInConv_ShouldReturnCount() {
        when(participantRepository.countNoOfAdminsInConv(1L)).thenReturn(3L);

        Long count = participantService.getAdminsCountInConv(1L);

        assertEquals(3L, count);
    }

    @Test
    void getMembersCountInConv_ShouldReturnCount() {
        when(participantRepository.countNoOfMembersInConv(1L)).thenReturn(10L);

        Long count = participantService.getMembersCountInConv(1L);

        assertEquals(10L, count);
    }

    @Test
    void isExists_ShouldReturnTrue() {
        when(participantRepository.existsByConversationIdAndUserId(1L, 2L)).thenReturn(true);

        boolean exists = participantService.isExists(1L, 2L);

        assertTrue(exists);
    }

    @Test
    void isExists_ShouldReturnFalse() {
        when(participantRepository.existsByConversationIdAndUserId(1L, 99L)).thenReturn(false);

        boolean exists = participantService.isExists(1L, 99L);

        assertFalse(exists);
    }
}
