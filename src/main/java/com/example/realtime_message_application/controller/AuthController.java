package com.example.realtime_message_application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.realtime_message_application.dto.auth.AuthenticationRequest;
import com.example.realtime_message_application.dto.auth.AuthenticationResponse;
import com.example.realtime_message_application.dto.user.UserDTO;
import com.example.realtime_message_application.dto.user.UserResponse;
import com.example.realtime_message_application.service.AuthService;
import com.example.realtime_message_application.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

        // @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        UserResponse userResponse = userService.addUser(userDTO);
        return ResponseEntity.ok().body("User registered successfully with ID: " + userResponse.userId());
    }
}
