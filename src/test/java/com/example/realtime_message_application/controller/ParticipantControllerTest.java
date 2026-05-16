package com.example.realtime_message_application.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import com.example.realtime_message_application.config.RateLimitingInterceptor;
import com.example.realtime_message_application.dto.conversation.ParticipantResponse;
import com.example.realtime_message_application.security.JwtAuthenticationFilter;
import com.example.realtime_message_application.security.JwtService;
import com.example.realtime_message_application.service.ParticipantService;
import com.example.realtime_message_application.service.RateLimitingService;

@WebMvcTest(ParticipantController.class)
@AutoConfigureMockMvc(addFilters = false)
class ParticipantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParticipantService participantService;

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

    @Test
    void getAllParticipantsInConv_ShouldReturnList() throws Exception {
        List<ParticipantResponse> responses = List.of(
                new ParticipantResponse(1L, 1L, 1L, "User1", "MEMBER", Instant.now()));
        when(participantService.getAllParticipantsInConv(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/participants/1/participants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void getAdminsCountInConv_ShouldReturnCount() throws Exception {
        when(participantService.getAdminsCountInConv(1L)).thenReturn(2L);

        mockMvc.perform(get("/api/v1/participants/1/admins"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void getMembersCountInConv_ShouldReturnCount() throws Exception {
        when(participantService.getMembersCountInConv(1L)).thenReturn(10L);

        mockMvc.perform(get("/api/v1/participants/1/members"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }
}
