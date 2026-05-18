package com.example.realtime_message_application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.realtime_message_application.component.RateLimitingInterceptor;
import com.example.realtime_message_application.dto.conversation.BlockingDTO;
import com.example.realtime_message_application.model.Block;
import com.example.realtime_message_application.security.JwtAuthenticationFilter;
import com.example.realtime_message_application.security.JwtService;
import com.example.realtime_message_application.service.BlockedService;
import com.example.realtime_message_application.service.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BlockController.class)
@AutoConfigureMockMvc(addFilters = false)
class BlockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlockedService blockedService;

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

    @Test
    void blockUser_ShouldReturnSuccess() throws Exception {
        BlockingDTO blockingDTO = new BlockingDTO(1L, 2L);

        mockMvc.perform(post("/api/block/blocking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blockingDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User blocked successfully."));
    }

    @Test
    void unblockUser_ShouldReturnSuccess() throws Exception {
        BlockingDTO blockingDTO = new BlockingDTO(1L, 2L);

        mockMvc.perform(post("/api/block/unblocking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blockingDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User unblocked successfully."));
    }

    @Test
    void getBlockedList_ShouldReturnList() throws Exception {
        when(blockedService.getBlockedList(1L)).thenReturn(List.of(new Block()));

        mockMvc.perform(get("/api/block/blocked-list/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }
}
