package com.example.realtime_message_application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.realtime_message_application.dto.message.ChatMessage;
import com.example.realtime_message_application.dto.message.DeleteMessage;
import com.example.realtime_message_application.dto.message.EditMessage;
import com.example.realtime_message_application.dto.message.MessageResponse;
import com.example.realtime_message_application.dto.message.PinMessage;
import com.example.realtime_message_application.dto.message.RestoreMessage;
import com.example.realtime_message_application.config.RateLimitingInterceptor;
import com.example.realtime_message_application.model.Message;
import com.example.realtime_message_application.security.JwtAuthenticationFilter;
import com.example.realtime_message_application.security.JwtService;
import com.example.realtime_message_application.service.MessageService;
import com.example.realtime_message_application.service.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean
    private RateLimitingService rateLimitingService;

    @MockBean
    private RateLimitingInterceptor rateLimitingInterceptor;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private Message message;
    private MessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        message = Message.builder().messageId(1L).content("Hello").build();
        messageResponse = MessageResponse.builder()
                .messageId(1L)
                .content("Hello")
                .senderId(1L)
                .conversationId(1L)
                .build();
    }

    @Test
    void getMessageById_ShouldReturnMessage() throws Exception {
        when(messageService.getMessageByIdWithDetails(1L)).thenReturn(message);

        mockMvc.perform(get("/api/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(1));
    }

    @Test
    void getMessageResponseById_ShouldReturnResponse() throws Exception {
        when(messageService.getMessageResponseById(1L)).thenReturn(messageResponse);

        mockMvc.perform(get("/api/messages/response/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(1));
    }

    @Test
    void getAllMessagesInConv_ShouldReturnList() throws Exception {
        when(messageService.getAllMessageInConversation(1L)).thenReturn(List.of(messageResponse));

        mockMvc.perform(get("/api/messages/conversation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void sendMessage_ShouldReturnResponse() throws Exception {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationId(1L);
        chatMessage.setSenderId(1L);
        chatMessage.setContent("Hello");
        when(messageService.sendMessageForActor(any(), any())).thenReturn(messageResponse);

        mockMvc.perform(post("/api/messages/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chatMessage)))
                .andExpect(status().isOk());
    }

    @Test
    void restoreMessage_ShouldReturnResponse() throws Exception {
        RestoreMessage restoreMessage = new RestoreMessage(1L, 1L);
        when(messageService.restoreMessage(restoreMessage)).thenReturn(messageResponse);

        mockMvc.perform(post("/api/messages/restore")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(restoreMessage)))
                .andExpect(status().isOk());
    }
}
