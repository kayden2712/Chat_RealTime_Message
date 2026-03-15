package com.example.realtime_message_application.service.user.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.realtime_message_application.dto.user.UserDTO;
import com.example.realtime_message_application.dto.user.UserResponse;
import com.example.realtime_message_application.dto.user.updateBio;
import com.example.realtime_message_application.mapper.UserMapper;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1L)
                .username("testuser")
                .password("password")
                .phoneNo("9876543210")
                .email("test@example.com")
                .online(true)
                .createdOn(Instant.now())
                .build();

        userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setPassword("password");
        userDTO.setPhoneNo("9876543210");
        userDTO.setEmail("test@example.com");

        userResponse = new UserResponse(1L, "testuser", "9876543210", "Nickname", Instant.now(), "Bio", "Online", "Online");
    }

    @Test
    void getAllUsers_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponse(any(), anyString())).thenReturn(userResponse);

        List<UserResponse> responses = userService.getAllUsers();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_ValidId_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(any(), anyString())).thenReturn(userResponse);

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals("testuser", response.username());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_InvalidId_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(1L));
    }

    @Test
    void findByUsername_ValidUsername_ShouldReturnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userMapper.toResponse(any(), anyString())).thenReturn(userResponse);

        UserResponse response = userService.findByUsername("testuser");

        assertNotNull(response);
        assertEquals("testuser", response.username());
    }

    @Test
    void findByUsername_InvalidUsername_ShouldThrowException() {
        when(userRepository.findByUsername("invalid")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> userService.findByUsername("invalid"));
    }

    @Test
    void addUser_ValidData_ShouldSaveUser() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNo(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.applyUser(any(), any())).thenReturn(user);
        when(userMapper.toResponse(any(), anyString())).thenReturn(userResponse);

        UserResponse response = userService.addUser(userDTO);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addUser_DuplicateUsername_ShouldThrowException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.addUser(userDTO));
    }

    @Test
    void deleteUserById_ValidId_ShouldDelete() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUserById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void disconnectUser_ValidId_ShouldUpdateStatus() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.disconnectUser(1L);

        assertFalse(user.isOnline());
        assertNotNull(user.getLastSeen());
        verify(userRepository).save(user);
    }

    @Test
    void updateBio_ValidData_ShouldUpdateBio() {
        updateBio bioDTO = new updateBio(1L,"New Bio");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.updateBio(bioDTO);

        assertEquals("New Bio", user.getBio());
    }
}
