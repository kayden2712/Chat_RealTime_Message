package com.example.realtime_message_application.service.impl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.realtime_message_application.dto.message.ReadReceiptDTO;
import com.example.realtime_message_application.dto.message.ReadReceiptResponse;
import com.example.realtime_message_application.model.Message;
import com.example.realtime_message_application.model.ReadReceipt;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.MessageRepository;
import com.example.realtime_message_application.repository.ReadReceiptRepository;
import com.example.realtime_message_application.service.UserService;

@ExtendWith(MockitoExtension.class)
class ReadReceiptServiceImplTest {

    @Mock
    private ReadReceiptRepository readReceiptRepo;

    @Mock
    private UserService userService;

    @Mock
    private MessageRepository messageRepo;

    @InjectMocks
    private ReadReceiptServiceImpl readReceiptService;

    private Message message;
    private User user;

    @BeforeEach
    void setUp() {
        message = Message.builder().messageId(10L).build();
        user = User.builder().userId(1L).username("reader").build();
    }

    @Test
    void markAsRead_Success_ShouldSaveAndReturnResponse() {
        ReadReceiptDTO dto = new ReadReceiptDTO(10L, 1L);
        when(messageRepo.findById(10L)).thenReturn(Optional.of(message));
        when(userService.getEntityByUserId(1L)).thenReturn(user);
        when(readReceiptRepo.existsByMessageIdAndReaderId(10L, 1L)).thenReturn(false);

        ReadReceipt savedReceipt = ReadReceipt.builder()
                .readReceiptId(100L)
                .message(message)
                .user(user)
                .build();
        when(readReceiptRepo.save(any(ReadReceipt.class))).thenReturn(savedReceipt);

        ReadReceiptResponse response = readReceiptService.markAsRead(dto);

        assertNotNull(response);
        assertEquals(100L, response.readReceiptId());
        assertEquals(10L, response.messageId());
        assertEquals("reader", response.readerName());
        verify(readReceiptRepo).save(any(ReadReceipt.class));
    }

    @Test
    void markAsRead_MessageNotFound_ShouldThrowException() {
        ReadReceiptDTO dto = new ReadReceiptDTO(99L, 1L);
        when(messageRepo.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> readReceiptService.markAsRead(dto));
        assertEquals("Message not found", ex.getMessage());
    }

    @Test
    void markAsRead_AlreadyRead_ShouldThrowException() {
        ReadReceiptDTO dto = new ReadReceiptDTO(10L, 1L);
        when(messageRepo.findById(10L)).thenReturn(Optional.of(message));
        when(userService.getEntityByUserId(1L)).thenReturn(user);
        when(readReceiptRepo.existsByMessageIdAndReaderId(10L, 1L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> readReceiptService.markAsRead(dto));
        assertEquals("Already read by this user", ex.getMessage());
    }

    @Test
    void getReadReceipts_Success_ShouldReturnList() {
        when(messageRepo.existsByMessageId(10L)).thenReturn(true);
        ReadReceipt receipt = ReadReceipt.builder()
                .readReceiptId(200L)
                .message(message)
                .user(user)
                .build();
        when(readReceiptRepo.findAllReadersForMessage(10L)).thenReturn(List.of(receipt));

        List<ReadReceiptResponse> responses = readReceiptService.getReadReceipts(10L);

        assertEquals(1, responses.size());
        assertEquals("reader", responses.get(0).readerName());
    }

    @Test
    void getReadReceipts_MessageNotFound_ShouldThrowException() {
        when(messageRepo.existsByMessageId(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> readReceiptService.getReadReceipts(99L));
        assertEquals("Message not found", ex.getMessage());
    }
}
