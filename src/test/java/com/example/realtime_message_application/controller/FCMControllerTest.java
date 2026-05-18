package com.example.realtime_message_application.controller;

import static org.mockito.ArgumentMatchers.eq;
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

import com.example.realtime_message_application.component.RateLimitingInterceptor;
import com.example.realtime_message_application.dto.notification.FCMTokenRequest;
import com.example.realtime_message_application.security.JwtAuthenticationFilter;
import com.example.realtime_message_application.security.JwtService;
import com.example.realtime_message_application.service.FCMService;
import com.example.realtime_message_application.service.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(FCMController.class)
@AutoConfigureMockMvc(addFilters = false)
class FCMControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FCMService fcmService;

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
    void registerToken_ShouldReturnSuccess() throws Exception {
        FCMTokenRequest request = new FCMTokenRequest(1L, "fcm-token-123");
        doNothing().when(fcmService).registerToken(eq(1L), eq("fcm-token-123"));

        mockMvc.perform(post("/api/fcm/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Token registered successfully"));
    }

    @Test
    void unregisterToken_ShouldReturnSuccess() throws Exception {
        doNothing().when(fcmService).deleteToken("fcm-token-123");

        mockMvc.perform(delete("/api/fcm/unregister")
                .param("token", "fcm-token-123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Token unregistered successfully"));
    }
}
