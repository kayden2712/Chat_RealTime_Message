package com.example.realtime_message_application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.realtime_message_application.dto.conversation.BanUserDTO;
import com.example.realtime_message_application.enums.ParticipantRole;
import com.example.realtime_message_application.model.BannedUser;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.model.ConversationParticipant;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.BanRepository;
import com.example.realtime_message_application.repository.ParticipantRepository;
import com.example.realtime_message_application.service.ConversationService;

@ExtendWith(MockitoExtension.class)
class BanServiceImplTest {

    @Mock
    private BanRepository banRepo;

    @Mock
    private ConversationService conversationService;

    @Mock
    private ParticipantRepository participantRepo;

    @InjectMocks
    private BanServiceImpl banService;

    private User adminUser;
    private User targetUser;
    private Conversation conversation;
    private ConversationParticipant adminParticipant;
    private ConversationParticipant targetParticipant;
    private BanUserDTO banUserDTO;

    @BeforeEach
    void setUp() {
        adminUser = User.builder().userId(1L).username("admin").build();
        targetUser = User.builder().userId(2L).username("target").build();
        conversation = Conversation.builder().conversationId(10L).build();
        
        adminParticipant = ConversationParticipant.builder()
                .user(adminUser)
                .conversation(conversation)
                .participantRole(ParticipantRole.ADMIN)
                .build();
                
        targetParticipant = ConversationParticipant.builder()
                .user(targetUser)
                .conversation(conversation)
                .participantRole(ParticipantRole.MEMBER)
                .build();

        banUserDTO = new BanUserDTO(10L, 2L, 1L, "Disruptive behavior");
    }

    @Test
    void banUser_Success_ShouldSaveBanRecord() {
        when(conversationService.getEntityByConvId(10L)).thenReturn(conversation);
        when(participantRepo.findByConversationAndUser(10L, 2L)).thenReturn(Optional.of(targetParticipant));
        when(participantRepo.findByConversationAndUser(10L, 1L)).thenReturn(Optional.of(adminParticipant));
        when(banRepo.existsByConvIdAndUserId(10L, 2L)).thenReturn(false);

        banService.banUser(banUserDTO);

        verify(participantRepo).deleteByConversation_ConversationIdAndUser_UserId(10L, 2L);
        verify(banRepo).save(any(BannedUser.class));
    }

    @Test
    void banUser_NonAdmin_ShouldThrowException() {
        adminParticipant.setParticipantRole(ParticipantRole.MEMBER);
        when(conversationService.getEntityByConvId(10L)).thenReturn(conversation);
        when(participantRepo.findByConversationAndUser(10L, 2L)).thenReturn(Optional.of(targetParticipant));
        when(participantRepo.findByConversationAndUser(10L, 1L)).thenReturn(Optional.of(adminParticipant));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> banService.banUser(banUserDTO));
        assertEquals("Admins can only ban users.", exception.getMessage());
    }

    @Test
    void banUser_AlreadyBanned_ShouldThrowException() {
        when(conversationService.getEntityByConvId(10L)).thenReturn(conversation);
        when(participantRepo.findByConversationAndUser(10L, 2L)).thenReturn(Optional.of(targetParticipant));
        when(participantRepo.findByConversationAndUser(10L, 1L)).thenReturn(Optional.of(adminParticipant));
        when(banRepo.existsByConvIdAndUserId(10L, 2L)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> banService.banUser(banUserDTO));
        assertEquals("User already banned from this conversation.", exception.getMessage());
    }

    @Test
    void unbanUser_Success_ShouldDeleteBanRecord() {
        BannedUser bannedUser = BannedUser.builder().build();
        when(banRepo.findByConvIdAndUserId(10L, 2L)).thenReturn(Optional.of(bannedUser));
        when(participantRepo.findByConversationAndUser(10L, 1L)).thenReturn(Optional.of(adminParticipant));

        banService.unbanUser(10L, 2L, 1L);

        verify(banRepo).delete(bannedUser);
    }

    @Test
    void unbanUser_NonAdmin_ShouldThrowException() {
        BannedUser bannedUser = BannedUser.builder().build();
        adminParticipant.setParticipantRole(ParticipantRole.MEMBER);
        when(banRepo.findByConvIdAndUserId(10L, 2L)).thenReturn(Optional.of(bannedUser));
        when(participantRepo.findByConversationAndUser(10L, 1L)).thenReturn(Optional.of(adminParticipant));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> banService.unbanUser(10L, 2L, 1L));
        assertEquals("Admins can only unban users.", exception.getMessage());
    }

    @Test
    void findByConversationAndUser_Found_ShouldReturnBannedUser() {
        BannedUser bannedUser = BannedUser.builder().build();
        when(banRepo.findByConvIdAndUserId(10L, 2L)).thenReturn(Optional.of(bannedUser));

        BannedUser result = banService.findByConversationAndUser(10L, 2L);

        assertNotNull(result);
        assertEquals(bannedUser, result);
    }

    @Test
    void findByConversationAndUser_NotFound_ShouldThrowException() {
        when(banRepo.findByConvIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> banService.findByConversationAndUser(10L, 2L));
        assertEquals("User not banned", exception.getMessage());
    }
}
