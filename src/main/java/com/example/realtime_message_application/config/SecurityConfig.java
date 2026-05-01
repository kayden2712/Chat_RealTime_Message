package com.example.realtime_message_application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.realtime_message_application.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {
                })
                .csrf(csrf -> csrf.disable()) // dung JWT khong luu session vao cookie

                // Quản lý Session: Không tạo session trên server
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // .authorizeHttpRequests(auth -> auth
                // // Cho phép các endpoint đăng ký/đăng nhập không cần token
                // .requestMatchers("/api/v1/auth/**").permitAll()
                // // Các request còn lại đều phải xác thực
                // .anyRequest().authenticated() )
                
                // Thêm Filter kiểm tra JWT trước khi xử lý các filter mặc định của Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.httpBasic(httpBasic -> {
        });
        return http.build();
    }
}
