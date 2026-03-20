package com.example.realtime_message_application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.realtime_message_application.dto.user.UserDTO;
import com.example.realtime_message_application.dto.user.UserResponse;
import com.example.realtime_message_application.dto.user.updateBio;
import com.example.realtime_message_application.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit tests
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse(1L, "testuser", "9876543210", "Nickname", Instant.now(), "Bio", "Online", "Online");
    }

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void findByUsername_ShouldReturnUser() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/user/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void deleteUserById_ShouldReturnSuccess() throws Exception {
        doNothing().when(userService).deleteUserById(1L);

        mockMvc.perform(delete("/api/v1/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully."));
    }

    @Test
    void disconnectUser_ShouldReturnSuccess() throws Exception {
        doNothing().when(userService).disconnectUser(1L);

        mockMvc.perform(put("/api/v1/user/disconnect/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User disconnected successfully."));
    }

    @Test
    void updateBio_ShouldReturnSuccess() throws Exception {
        updateBio bioDTO = new updateBio(1L, "New Bio");
        doNothing().when(userService).updateBio(any(updateBio.class));

        mockMvc.perform(put("/api/v1/user/update/bio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bioDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Bio updated successfully."));
    }
}
