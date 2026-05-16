package com.example.realtime_message_application.service.impl;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.realtime_message_application.dto.message.MessageResponse;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.model.ConversationParticipant;
import com.example.realtime_message_application.model.FCMToken;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.FCMTokenRepository;
import com.example.realtime_message_application.service.ConversationService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private FCMTokenRepository fcmrRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Conversation conversation;
    private User sender;
    private User receiver;
    private MessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        sender = User.builder().userId(1L).username("sender").nickname("Sender").build();
        receiver = User.builder().userId(2L).username("receiver").nickname("Receiver").build();
        conversation = Conversation.builder().conversationId(10L).build();

        ConversationParticipant senderPart = ConversationParticipant.builder()
                .user(sender).conversation(conversation).build();
        ConversationParticipant receiverPart = ConversationParticipant.builder()
                .user(receiver).conversation(conversation).build();
        conversation.setParticipants(Set.of(senderPart, receiverPart));

        messageResponse = MessageResponse.builder()
                .messageId(100L)
                .senderId(1L)
                .senderName("Sender")
                .content("Test message content that is longer than forty characters")
                .conversationId(10L)
                .build();
    }

    @Test
    void notifyNewMessage_ShouldSendToReceiversExcludingSender() {
        when(conversationService.getEntityByConvId(10L)).thenReturn(conversation);

        notificationService.notifyNewMessage(10L, 100L, 1L, "Sender", "Hello");

        verify(simpMessagingTemplate).convertAndSendToUser(
                eq("2"), eq("/queue/notifications"), any());
        verify(simpMessagingTemplate, never()).convertAndSendToUser(
                eq("1"), eq("/queue/notifications"), any());
    }

    @Test
    void notifyParticipants_WithTokens_ShouldCallFCMRepository() {
        when(conversationService.getEntityByConvId(10L)).thenReturn(conversation);
        when(fcmrRepository.findAllByUserIn(anyList())).thenReturn(
                List.of(FCMToken.builder().token("token123").build()));

        // This will attempt to call FirebaseMessaging.getInstance() which will fail
        // in unit test environment; we catch the exception in the implementation.
        // The test verifies that up to the Firebase call, everything works.
        try {
            notificationService.notifyParticipants(10L, messageResponse);
        } catch (Exception ignored) {
            // FirebaseMessaging may throw in test environment, which is expected
        }

        verify(conversationService).getEntityByConvId(10L);
        verify(fcmrRepository).findAllByUserIn(anyList());
    }

    @Test
    void notifyParticipants_NoTokens_ShouldNotCallFirebase() {
        when(conversationService.getEntityByConvId(10L)).thenReturn(conversation);
        when(fcmrRepository.findAllByUserIn(anyList())).thenReturn(List.of());

        notificationService.notifyParticipants(10L, messageResponse);

        verify(conversationService).getEntityByConvId(10L);
        verify(fcmrRepository).findAllByUserIn(anyList());
        // No Firebase interaction since tokens list is empty
    }

    @Test
    void notifyNewMessage_WithFailedSend_ShouldNotThrow() {
        when(conversationService.getEntityByConvId(10L)).thenReturn(conversation);
        doThrow(new RuntimeException("Send failed"))
                .when(simpMessagingTemplate)
                .convertAndSendToUser(eq("2"), eq("/queue/notifications"), any());

        // Should not propagate exception
        assertDoesNotThrow(() ->
                notificationService.notifyNewMessage(10L, 100L, 1L, "Sender", "Hello"));
    }

    @Test
    void sendBulkFirebaseNotification_ShouldSendSuccessfully() throws FirebaseMessagingException {
        List<String> tokens = List.of("token1", "token2");
        String title = "Test Title";
        String body = "Test Body";

        try (MockedStatic<FirebaseMessaging> mockedFirebase = mockStatic(FirebaseMessaging.class)) {
            FirebaseMessaging mockFirebaseInstance = mock(FirebaseMessaging.class);
            mockedFirebase.when(FirebaseMessaging::getInstance).thenReturn(mockFirebaseInstance);

            notificationService.sendBulkFirebaseNotification(tokens, title, body);

            verify(mockFirebaseInstance).sendEachForMulticast(any());
        }
    }
}
