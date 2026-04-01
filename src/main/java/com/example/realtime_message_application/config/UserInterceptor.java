package com.example.realtime_message_application.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // Lấy bộ truy cập Header để đọc thông tin từ tin nhắn STOMP
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Kiểm tra xem đây có phải là yêu cầu KẾT NỐI (CONNECT)
        if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {

            // Lấy giá trị của header có tên là "login" do phía Client gửi lên
            String login = headerAccessor.getFirstNativeHeader("login");

            if (login != null) {
                // Thiết lập Principal cho WebSocket session
                headerAccessor.setUser(() -> login);
            }
        }
        return message;
    }
}
