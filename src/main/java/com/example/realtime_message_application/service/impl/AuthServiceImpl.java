package com.example.realtime_message_application.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.realtime_message_application.dto.auth.AuthenticationRequest;
import com.example.realtime_message_application.dto.auth.AuthenticationResponse;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.UserRepository;
import com.example.realtime_message_application.security.JwtService;
import com.example.realtime_message_application.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            log.info("Attempting to authenticate user: {}", request.getUsername());

            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            log.info("Authentication successful for user: {}", request.getUsername());

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details from authentication
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            log.info("User details retrieved. Principal type: {}", userDetails.getClass().getName());

            // Generate JWT token
            String token = jwtService.generateToken(userDetails);

            // Get user from database for additional info
            User user = userRepository.loadByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found in database"));

            log.info("User retrieved from database: {} (ID: {})", user.getUsername(), user.getUserId());

            return AuthenticationResponse.builder()
                    .token(token)
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build();
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getUsername(), e);
            throw e;
        }
    }
}
