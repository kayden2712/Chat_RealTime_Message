package com.example.realtime_message_application.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.realtime_message_application.dto.auth.AuthenticationRequest;
import com.example.realtime_message_application.dto.auth.AuthenticationResponse;
import com.example.realtime_message_application.exception.UnauthorizedException;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.UserRepository;
import com.example.realtime_message_application.security.CustomUserDetails;
import com.example.realtime_message_application.security.JwtService;
import com.example.realtime_message_application.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Đang xử lý đăng nhập cho: {}", request.getUsername());
        try {
            User user = userRepository.loadByUsername(request.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("Đăng nhập thất bại: Sai mật khẩu cho tài khoản '{}'", request.getUsername());
                throw new UnauthorizedException("Invalid username or password");
            }

            log.info("Xác thực thành công! Đang tạo Access Token cho: {}", user.getUsername());

            UserDetails userDetails = new CustomUserDetails(user);

            String accessToken = jwtService.generateToken(userDetails);
            return AuthenticationResponse.builder()
                    .token(accessToken)
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
