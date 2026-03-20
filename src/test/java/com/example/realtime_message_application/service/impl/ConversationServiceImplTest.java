package com.example.realtime_message_application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import com.example.realtime_message_application.dto.conversation.AddParticipant;
import com.example.realtime_message_application.enums.ConversationType;
import com.example.realtime_message_application.enums.ParticipantRole;
import com.example.realtime_message_application.mapper.ConversationMapper;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.model.ConversationParticipant;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.ConversationRepository;
import com.example.realtime_message_application.repository.MessageRepository;
import com.example.realtime_message_application.repository.ParticipantRepository;
import com.example.realtime_message_application.service.UserService;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private UserService userService;
    @Mock
    private ConversationMapper conversationMapper;
    @Mock
    private TaskScheduler taskScheduler;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private User admin;
    private User member;
    private Conversation groupConv;
    private Conversation privateConv;
    private ConversationParticipant adminParticipant;

    @BeforeEach
    void setUp() {
        admin = User.builder().userId(1L).username("admin").build();
        member = User.builder().userId(2L).username("member").build();

        groupConv = Conversation.builder()
                .conversationId(10L)
                .type(ConversationType.GROUP)
                .build();

        privateConv = Conversation.builder()
                .conversationId(11L)
                .type(ConversationType.PRIVATE)
                .build();

        adminParticipant = ConversationParticipant.builder()
                .user(admin)
                .conversation(groupConv)
                .participantRole(ParticipantRole.ADMIN)
                .build();
    }

    @Test
    void AddParticipantInConversation_Success_ShouldAddMember() {
        AddParticipant addParticipant = new AddParticipant(10L, 2L, 1L, ParticipantRole.MEMBER);

        when(conversationRepository.findById(10L)).thenReturn(Optional.of(groupConv));
        when(userService.getEntityByUserId(2L)).thenReturn(member);
        when(userService.getEntityByUserId(1L)).thenReturn(admin);
        when(participantRepository.findByConversationAndUser(10L, 2L)).thenReturn(Optional.empty());

        conversationService.AddParticipantInConversation(addParticipant);

        verify(participantRepository).save(any(ConversationParticipant.class));
    }

    @Test
    void AddParticipantInConversation_PrivateChat_ShouldThrowException() {
        AddParticipant addParticipant = new AddParticipant(11L, 2L, 1L, ParticipantRole.MEMBER);

        when(conversationRepository.findById(11L)).thenReturn(Optional.of(privateConv));
        when(userService.getEntityByUserId(2L)).thenReturn(member);
        when(userService.getEntityByUserId(1L)).thenReturn(admin);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> conversationService.AddParticipantInConversation(addParticipant));
        assertTrue(exception.getMessage().contains("ONE to ONE chat"));
    }

    @Test
    void createPrivateGroup_NewConversation_ShouldCreate() {
        when(conversationRepository.findPrivateConvBetweenTwoUsers(1L, 2L)).thenReturn(Optional.empty());
        when(userService.getEntityByUserId(1L)).thenReturn(admin);
        when(userService.getEntityByUserId(2L)).thenReturn(member);
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(i -> i.getArgument(0));

        conversationService.createPrivateGroup(1L, 2L);

        verify(conversationRepository).save(any(Conversation.class));
        verify(participantRepository, times(2)).save(any(ConversationParticipant.class));
    }

    @Test
    void createPrivateGroup_SelfConversation_ShouldCreateCloudChat() {
        when(conversationRepository.findSelfConversation(1L)).thenReturn(Optional.empty());
        when(userService.getEntityByUserId(1L)).thenReturn(admin);
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(i -> i.getArgument(0));

        conversationService.createPrivateGroup(1L, 1L);

        verify(conversationRepository).save(any(Conversation.class));
        verify(participantRepository).save(any(ConversationParticipant.class));
    }

    @Test
    void leaveConversation_Success_ShouldDeleteParticipant() {
        com.example.realtime_message_application.dto.conversation.LeaveConversation leaveDTO = new com.example.realtime_message_application.dto.conversation.LeaveConversation(
                10L, 1L);

        when(participantRepository.findByConversationAndUser(10L, 1L)).thenReturn(Optional.of(adminParticipant));

        String result = conversationService.leaveConversation(leaveDTO);

        assertEquals("User left the conversation", result);
        verify(participantRepository).delete(adminParticipant);
    }
}
