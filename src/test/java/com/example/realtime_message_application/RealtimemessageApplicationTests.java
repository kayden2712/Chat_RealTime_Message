package com.example.realtime_message_application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.example.realtime_message_application.config.RateLimitingInterceptor;
import com.example.realtime_message_application.security.JwtAuthenticationFilter;
import com.example.realtime_message_application.security.JwtService;
import com.example.realtime_message_application.service.PresenceService;
import com.example.realtime_message_application.service.RateLimitingService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@SpringBootTest
class RealtimemessageApplicationTests {

	@MockBean
	private RateLimitingService rateLimitingService;

	@MockBean
	private RateLimitingInterceptor rateLimitingInterceptor;

	@MockBean
	private JwtService jwtService;

	@MockBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@MockBean
	private PresenceService presenceService;

	@MockBean
	private SimpMessagingTemplate messagingTemplate;

	@MockBean
	private RedisConnectionFactory redisConnectionFactory;

	@Test
	void contextLoads() {
	}

}
