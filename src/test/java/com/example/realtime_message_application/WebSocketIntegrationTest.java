package com.example.realtime_message_application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.example.realtime_message_application.component.FCMInitializer;
import com.example.realtime_message_application.component.RateLimitingInterceptor;
import com.example.realtime_message_application.dto.message.ChatMessage;
import com.example.realtime_message_application.dto.message.MessageResponse;
import com.example.realtime_message_application.enums.ConversationType;
import com.example.realtime_message_application.enums.MessageType;
import com.example.realtime_message_application.enums.ParticipantRole;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.model.ConversationParticipant;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.ConversationRepository;
import com.example.realtime_message_application.repository.ParticipantRepository;
import com.example.realtime_message_application.repository.UserRepository;
import com.example.realtime_message_application.security.JwtHandshakeInterceptor;
import com.example.realtime_message_application.security.JwtService;
import com.example.realtime_message_application.security.SecurityUtils;
import com.example.realtime_message_application.service.NotificationService;
import com.example.realtime_message_application.service.PresenceService;
import com.example.realtime_message_application.service.RateLimitingService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private static final String WEBSOCKET_URI = "http://localhost:%d/ws";
    private static final String WEBSOCKET_TOPIC = "/topic/conversations.%s";

    private BlockingQueue<MessageResponse> blockingQueue;

    @MockBean
    private FCMInitializer fcmInitializer;

    @MockBean
    private RateLimitingService rateLimitingService;

    @MockBean
    private RateLimitingInterceptor rateLimitingInterceptor;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PresenceService presenceService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    private MockedStatic<SecurityUtils> mockedSecurity;

    @BeforeEach
    public void setup() throws Exception {
        // Allow WebSocket handshake (mock default returns false, blocking upgrade)
        when(jwtHandshakeInterceptor.beforeHandshake(any(), any(), any(), any())).thenReturn(true);

        // Pass through messages instead of returning null (mock default)
        when(rateLimitingInterceptor.preSend(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Allow all rate-limited requests
        when(rateLimitingService.tryConsume(any(), anyLong())).thenReturn(true);

        // Build STOMP-over-SockJS client
        stompClient = new WebSocketStompClient(new SockJsClient(
                java.util.List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        blockingQueue = new LinkedBlockingDeque<>();

        // Create test data in H2
        User user = User.builder()
                .username("testuser")
                .password("password")
                .phoneNo("1234567890")
                .email("test@example.com")
                .build();
        user = userRepository.save(user);

        Conversation conversation = Conversation.builder()
                .type(ConversationType.GROUP)
                .title("Test Conversation")
                .creator(user)
                .build();
        conversation = conversationRepository.save(conversation);

        ConversationParticipant participant = ConversationParticipant.builder()
                .conversation(conversation)
                .user(user)
                .participantRole(ParticipantRole.ADMIN)
                .build();
        participantRepository.save(participant);

        // Stub SecurityUtils so controller resolves the authenticated user
        mockedSecurity = mockStatic(SecurityUtils.class);
        mockedSecurity.when(SecurityUtils::getUserId).thenReturn(user.getUserId());
    }

    @AfterEach
    public void tearDown() {
        if (mockedSecurity != null) {
            mockedSecurity.close();
        }
    }

    @Test
    public void testWebSocketSendMessage() throws Exception {
        // Connect
        StompSession session = stompClient.connect(
                String.format(WEBSOCKET_URI, port),
                new StompSessionHandlerAdapter() {
                })
                .get(10, TimeUnit.SECONDS);

        // Subscribe to the group conversation topic (note the dot separator)
        session.subscribe(String.format(WEBSOCKET_TOPIC, "1"), new DefaultStompFrameHandler());

        // Send a chat message over STOMP
        ChatMessage message = new ChatMessage();
        message.setContent("Hello from test");
        message.setSenderId(1L);
        message.setConversationId(1L);
        message.setType(MessageType.TEXT);
        session.send("/app/chat.send", message);

        // Verify the message was echoed back to the topic
        MessageResponse receivedMessage = blockingQueue.poll(5, TimeUnit.SECONDS);
        assertNotNull(receivedMessage, "Expected to receive a message within the timeout");
        assertEquals("Hello from test", receivedMessage.getContent());
        assertEquals(1L, receivedMessage.getSenderId());

        session.disconnect();
    }

    private class DefaultStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return MessageResponse.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object payload) {
            blockingQueue.offer((MessageResponse) payload);
        }
    }
}
