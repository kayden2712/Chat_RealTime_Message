package com.example.realtime_message_application.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.example.realtime_message_application.security.JwtHandshakeInterceptor;

import lombok.RequiredArgsConstructor;

@Configurable
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final UserInterceptor userInterceptor;
    private final RateLimitingInterceptor rateLimitingInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        // nơi Server định nghĩa các "vùng nhận tin"
        // Cứ mỗi 1 giây, Client và Server sẽ "vẫy tay" chào nhau một cái để báo rằng
        // "Tôi vẫn còn sống"
        config.enableSimpleBroker("/topic", "/queue").setHeartbeatValue(new long[] { 1000, 1000 });
        // tiền tố cho các API gửi tin nhắn
        config.setApplicationDestinationPrefixes("/app");
        // tiền tố cho các tin nhắn gửi riêng cho từng user
        config.setUserDestinationPrefix("/user");

    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("http://127.0.0.1:5500", "http://localhost:3000")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(userInterceptor, rateLimitingInterceptor);
    }

}
