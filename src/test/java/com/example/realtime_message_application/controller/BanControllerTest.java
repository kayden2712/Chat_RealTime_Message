package com.example.realtime_message_application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.realtime_message_application.config.RateLimitingInterceptor;
import com.example.realtime_message_application.dto.conversation.BanUserDTO;
import com.example.realtime_message_application.security.JwtAuthenticationFilter;
import com.example.realtime_message_application.security.JwtService;
import com.example.realtime_message_application.service.BanService;
import com.example.realtime_message_application.service.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BanController.class)
@AutoConfigureMockMvc(addFilters = false)
class BanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BanService banService;

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
    void banUser_ShouldReturnSuccess() throws Exception {
        BanUserDTO banUserDTO = new BanUserDTO(1L, 2L, 1L, "Reason");
        doNothing().when(banService).banUser(any(BanUserDTO.class));

        mockMvc.perform(post("/api/ban/ban")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(banUserDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User banned successfully."));
    }

    @Test
    void unbanUser_ShouldReturnSuccess() throws Exception {
        BanUserDTO unbanUserDTO = new BanUserDTO(1L, 2L, 1L, null);
        doNothing().when(banService).unbanUser(any(BanUserDTO.class));

        mockMvc.perform(post("/api/ban/unban")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unbanUserDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User unbanned successfully."));
    }
}
