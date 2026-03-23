package com.example.realtime_message_application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.example.realtime_message_application.dto.message.MessageCommands.DeleteMessageCommand;
import com.example.realtime_message_application.dto.message.MessageCommands.EditMessageCommand;
import com.example.realtime_message_application.dto.message.MessageCommands.PinMessageCommand;
import com.example.realtime_message_application.dto.message.MessageCommands.SendMessageCommand;
import com.example.realtime_message_application.dto.message.MessageResponse;
import com.example.realtime_message_application.dto.message.RestoreMessage;
import com.example.realtime_message_application.enums.ConversationType;
import com.example.realtime_message_application.enums.MessageType;
import com.example.realtime_message_application.mapper.MessageMapper;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.model.ConversationParticipant;
import com.example.realtime_message_application.model.Message;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.BanRepository;
import com.example.realtime_message_application.repository.BlockRepository;
import com.example.realtime_message_application.repository.ConversationRepository;
import com.example.realtime_message_application.repository.MessageRepository;
import com.example.realtime_message_application.service.ConversationService;
import com.example.realtime_message_application.service.ParticipantService;
import com.example.realtime_message_application.service.UserService;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ConversationService conversationService;
    @Mock
    private UserService userService;
    @Mock
    private MessageMapper messageMapper;
    @Mock
    private BanRepository banRepository;
    @Mock
    private BlockRepository blockRepository;
    @Mock
    private ParticipantService participantService;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Message message;
    private User sender;
    private Conversation conversation;
    private MessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .userId(1L)
                .username("sender")
                .nickname("Sender")
                .build();

        conversation = Conversation.builder()
                .conversationId(1L)
                .type(ConversationType.PRIVATE)
                .build();

        message = Message.builder()
                .messageId(1L)
                .content("Hello")
                .sender(sender)
                .conversation(conversation)
                .replies(new HashSet<>())
                .isDeleted(false)
                .messageType(MessageType.TEXT)
                .build();

        messageResponse = MessageResponse.builder()
                .messageId(1L)
                .content("Hello")
                .senderId(1L)
                .conversationId(1L)
                .build();
    }

    @Test
    void getMessageById_ValidId_ShouldReturnMessage() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        Message result = messageService.getMessageById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getMessageId());
    }

    @Test
    void getAllMessageInConversation_ValidConversation_ShouldReturnList() {
        when(conversationRepository.existsById(1L)).thenReturn(true);
        when(messageRepository.findAllMessagesByConversationId(1L)).thenReturn(List.of(message));
        when(messageMapper.toMessageResponse(any())).thenReturn(messageResponse);

        List<MessageResponse> result = messageService.getAllMessageInConversation(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void restoreMessage_ValidData_ShouldRestore() {
        RestoreMessage restoreDTO = new RestoreMessage(1L, 1L);
        message.setDeleted(true);
        message.setDeletedBy("Someone");

        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(userService.isExists(1L)).thenReturn(true);
        when(messageMapper.toMessageResponse(message)).thenReturn(messageResponse);

        MessageResponse result = messageService.restoreMessage(restoreDTO);

        assertNotNull(result);
        assertFalse(message.isDeleted());
        assertNull(message.getDeletedBy());
    }

    @Test
    void deleteMessage_ValidSender_ShouldDelete() {
        DeleteMessageCommand command = new DeleteMessageCommand(1L, 1L, 1L);
        when(messageRepository.findByMessageIdWithDetails(1L)).thenReturn(Optional.of(message));
        when(userService.isExists(1L)).thenReturn(true);
        when(participantService.isExists(1L, 1L)).thenReturn(true);

        messageService.deleteMessage(command);

        assertNotNull(message.getDeletedBy());
        verify(messageRepository).delete(message);
    }

    @Test
    void pinMessage_ValidData_ShouldPin() {
        PinMessageCommand command = new PinMessageCommand(1L, 1L, 1L);
        when(conversationRepository.existsById(1L)).thenReturn(true);
        when(userService.isExists(1L)).thenReturn(true);
        when(banRepository.existsByConvIdAndUserId(1L, 1L)).thenReturn(true);
        when(participantService.isExists(1L, 1L)).thenReturn(true);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(conversationRepository.countNoOfPinnedMessageConv(1L)).thenReturn(5);
        when(messageMapper.toMessageResponse(message)).thenReturn(messageResponse);

        MessageResponse result = messageService.pinMessage(command);

        assertTrue(message.isPinned());
        assertNotNull(message.getPinnedBy());
        assertNotNull(result);
    }

    @Test
    void editMessage_ValidSender_ShouldUpdateContent() {
        EditMessageCommand command = new EditMessageCommand(1L, 1L, 1L, "New Content");
        when(conversationRepository.existsById(1L)).thenReturn(true);
        when(userService.isExists(1L)).thenReturn(true);
        when(banRepository.existsByConvIdAndUserId(1L, 1L)).thenReturn(true);
        when(participantService.isExists(1L, 1L)).thenReturn(true);
        when(messageRepository.findByMessageIdWithDetails(1L)).thenReturn(Optional.of(message));
        when(messageMapper.toMessageResponse(message)).thenReturn(messageResponse);

        MessageResponse result = messageService.editMessage(command);

        assertEquals("New Content", message.getContent());
        assertNotNull(message.getEditedAt());
    }

    @Test
    void sendMessage_Text_ShouldSaveMessage() {
        SendMessageCommand command = new SendMessageCommand(1L, 1L, "Hello", MessageType.TEXT, null, null);
        
        User receiver = User.builder().userId(2L).username("receiver").build();
        conversation.setParticipants(Set.of(
            ConversationParticipant.builder()
                .conversation(conversation)
                .user(sender)
                .participantRole(com.example.realtime_message_application.enums.ParticipantRole.MEMBER)
                .build(),
            ConversationParticipant.builder()
                .conversation(conversation)
                .user(receiver)
                .participantRole(com.example.realtime_message_application.enums.ParticipantRole.MEMBER)
                .build()
        ));

        when(conversationService.getEntityByConvId(1L)).thenReturn(conversation);
        when(userService.getEntityByUserId(1L)).thenReturn(sender);
        when(participantService.isExists(1L, 1L)).thenReturn(true);
        when(blockRepository.existsByBlockerAndBlocked(2L, 1L)).thenReturn(false);
        when(blockRepository.existsByBlockerAndBlocked(1L, 2L)).thenReturn(false);
        when(banRepository.existsByConvIdAndUserId(1L, 1L)).thenReturn(false);
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(messageMapper.toMessageResponse(any(Message.class))).thenReturn(messageResponse);

        MessageResponse result = messageService.sendMessage(command);

        assertNotNull(result);
        verify(messageRepository).save(any(Message.class));
    }
}
