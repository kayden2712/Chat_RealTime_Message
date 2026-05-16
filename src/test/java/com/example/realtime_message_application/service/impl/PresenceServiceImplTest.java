package com.example.realtime_message_application.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.model.ConversationParticipant;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.service.ConversationService;

@ExtendWith(MockitoExtension.class)
class PresenceServiceImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private PresenceServiceImpl presenceService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void isOnline_UserOnline_ShouldReturnTrue() {
        when(redisTemplate.hasKey("user:online:1")).thenReturn(true);

        boolean online = presenceService.isOnline(1L);

        assertTrue(online);
    }

    @Test
    void isOnline_UserOffline_ShouldReturnFalse() {
        when(redisTemplate.hasKey("user:online:2")).thenReturn(false);

        boolean online = presenceService.isOnline(2L);

        assertFalse(online);
    }

    @Test
    void getFriendsStatus_ShouldReturnMixedStatuses() {
        when(redisTemplate.hasKey("user:online:1")).thenReturn(true);
        when(redisTemplate.hasKey("user:online:2")).thenReturn(false);

        Map<Long, String> statuses = presenceService.getFriendsStatus(List.of(1L, 2L));

        assertEquals(2, statuses.size());
        assertEquals("ONLINE", statuses.get(1L));
        assertEquals("OFFLINE", statuses.get(2L));
    }

    @Test
    void markOnline_ShouldSetKeyWithTimeout() {
        presenceService.markOnline(1L);

        verify(valueOperations).set(eq("user:online:1"), eq("ONLINE"), eq(Duration.ofSeconds(30)));
    }

    @Test
    void markOffline_ShouldDeleteKey() {
        presenceService.markOffline(1L);

        verify(redisTemplate).delete("user:online:1");
    }

    @Test
    void refreshOnline_ShouldExpireKey() {
        presenceService.refreshOnline(1L);

        verify(redisTemplate).expire(eq("user:online:1"), eq(Duration.ofSeconds(30)));
    }

    @Test
    void getOnlineUserByConvId_ShouldReturnOnlineUsers() {
        Conversation conversation = Conversation.builder().conversationId(1L).build();
        User user1 = User.builder().userId(1L).build();
        User user2 = User.builder().userId(2L).build();
        ConversationParticipant p1 = ConversationParticipant.builder()
                .user(user1).conversation(conversation).build();
        ConversationParticipant p2 = ConversationParticipant.builder()
                .user(user2).conversation(conversation).build();
        conversation.setParticipants(Set.of(p1, p2));

        when(conversationService.getEntityByConvId(1L)).thenReturn(conversation);
        when(redisTemplate.hasKey("user:online:1")).thenReturn(true);
        when(redisTemplate.hasKey("user:online:2")).thenReturn(false);

        List<Long> onlineUsers = presenceService.getOnlineUserByConvId(1L);

        assertEquals(1, onlineUsers.size());
        assertTrue(onlineUsers.contains(1L));
    }

    @Test
    void getOnlineUserByConvId_ConvNotFound_ShouldReturnEmptyList() {
        when(conversationService.getEntityByConvId(99L)).thenReturn(null);

        List<Long> onlineUsers = presenceService.getOnlineUserByConvId(99L);

        assertTrue(onlineUsers.isEmpty());
    }
}
