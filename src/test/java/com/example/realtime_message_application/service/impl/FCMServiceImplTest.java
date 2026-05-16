package com.example.realtime_message_application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.realtime_message_application.model.FCMToken;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.FCMTokenRepository;
import com.example.realtime_message_application.service.UserService;

@ExtendWith(MockitoExtension.class)
class FCMServiceImplTest {

    @Mock
    private FCMTokenRepository fcmTokenRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private FCMServiceImpl fcmService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .username("testuser")
                .build();
    }

    @Test
    void registerToken_Success_ShouldSaveNewToken() {
        String token = "fcm-token-123";
        when(userService.getEntityByUserId(1L)).thenReturn(user);
        when(fcmTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        fcmService.registerToken(1L, token);

        verify(fcmTokenRepository).save(any(FCMToken.class));
    }

    @Test
    void registerToken_NullToken_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> fcmService.registerToken(1L, null));
    }

    @Test
    void registerToken_BlankToken_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> fcmService.registerToken(1L, "   "));
    }

    @Test
    void registerToken_ExistingToken_ShouldSkipRegistration() {
        String token = "existing-token";
        when(userService.getEntityByUserId(1L)).thenReturn(user);
        when(fcmTokenRepository.findByUser(user)).thenReturn(
                Optional.of(FCMToken.builder().user(user).token(token).build()));

        fcmService.registerToken(1L, token);

        verify(fcmTokenRepository, never()).save(any());
    }

    @Test
    void getUserTokens_ShouldReturnTokenList() {
        when(fcmTokenRepository.findByUser_UserId(1L)).thenReturn(
                List.of(FCMToken.builder().token("t1").build(),
                        FCMToken.builder().token("t2").build()));

        List<String> tokens = fcmService.getUserTokens(1L);

        assertEquals(2, tokens.size());
        assertTrue(tokens.contains("t1"));
    }

    @Test
    void deleteToken_ShouldCallRepository() {
        fcmService.deleteToken("token-to-delete");

        verify(fcmTokenRepository).deleteByToken("token-to-delete");
    }
}
