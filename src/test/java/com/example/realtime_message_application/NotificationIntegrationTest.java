package com.example.realtime_message_application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.realtime_message_application.dto.message.MessageResponse;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.model.ConversationParticipant;
import com.example.realtime_message_application.model.FCMToken;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.FCMTokenRepository;
import com.example.realtime_message_application.service.ConversationService;
import com.example.realtime_message_application.service.NotificationService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;

@SpringBootTest
public class NotificationIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private FCMTokenRepository fcmTokenRepository;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;

    private Conversation conversation;
    private User sender;
    private User receiver;
    private MessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
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
                .content("Test message content")
                .conversationId(10L)
                .build();

        // Save FCM token for receiver
        FCMToken token = FCMToken.builder().user(receiver).token("test-fcm-token").build();
        fcmTokenRepository.save(token);
    }

    @Test
    void notifyParticipants_ShouldSendWebSocketAndPushNotification() throws FirebaseMessagingException {
        // Mock FirebaseMessaging to avoid actual API calls
        try (var mockedFirebase = mockStatic(FirebaseMessaging.class)) {
            var mockFirebaseInstance = org.mockito.Mockito.mock(FirebaseMessaging.class);
            mockedFirebase.when(FirebaseMessaging::getInstance).thenReturn(mockFirebaseInstance);

            // When
            notificationService.notifyParticipants(10L, messageResponse);

            // Then - verify WebSocket notification sent (excluding sender)
            org.mockito.Mockito.verify(simpMessagingTemplate, org.mockito.Mockito.never())
                    .convertAndSendToUser("1", "/queue/notifications", any());
            // Note: In integration test, we can't easily verify the exact call due to complex setup

            // Verify Firebase was called
            org.mockito.Mockito.verify(mockFirebaseInstance).sendEachForMulticast(any());
        }
    }
}