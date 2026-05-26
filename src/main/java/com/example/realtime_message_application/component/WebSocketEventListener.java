package com.example.realtime_message_application.component;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.realtime_message_application.service.PresenceService;
import com.example.realtime_message_application.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        // Giả sử bạn truyền userId qua header khi connect
        String userIdStr = headerAccessor.getFirstNativeHeader("userId");

        if (userIdStr != null) {
            Long userId = Long.valueOf(userIdStr);
            presenceService.markOnline(userId);

            // Thông báo cho bạn bè là "Tôi vừa online"
            broadcastStatus(userId, "ONLINE");
            log.info("User connected: {}", userId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userIdStr = (String) headerAccessor.getSessionAttributes().get("userId");

        if (userIdStr != null) {
            Long userId = Long.valueOf(userIdStr);
            presenceService.markOffline(userId);
            try {
                userService.disconnectUser(userId);
            } catch (Exception e) {
                log.error("Failed to update lastSeen for user {}", userId, e);
            }
            // Thông báo cho bạn bè là "Tôi vừa offline"
            broadcastStatus(userId, "OFFLINE");
            log.info("User disconnected: {}", userId);
        }
    }

    private void broadcastStatus(Long userId, String status) {
        // Topic này tất cả bạn bè của user này sẽ subcribe để nhận tin
        // Ví dụ: /topic/friends/status
        Map<String, Object> statusUpdate = Map.of(
                "userId", userId,
                "status", status);
        messagingTemplate.convertAndSend("/topic/public.status", statusUpdate);
    }
}