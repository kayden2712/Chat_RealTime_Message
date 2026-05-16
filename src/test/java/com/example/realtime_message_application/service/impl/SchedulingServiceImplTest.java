package com.example.realtime_message_application.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.realtime_message_application.model.Message;
import com.example.realtime_message_application.repository.MessageRepository;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private SchedulingServiceImpl schedulingService;

    @Test
    void deleteExpiredMessages_HasExpired_ShouldDeleteMessages() {
        Message expiredMsg = Message.builder().messageId(1L).build();
        when(messageRepository.findExpiredMessages(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredMsg));

        schedulingService.deleteExpiredMessages();

        verify(messageRepository).delete(expiredMsg);
    }

    @Test
    void deleteExpiredMessages_NoExpired_ShouldNotDelete() {
        when(messageRepository.findExpiredMessages(any(LocalDateTime.class)))
                .thenReturn(List.of());

        schedulingService.deleteExpiredMessages();

        verify(messageRepository, never()).delete(any());
    }
}
