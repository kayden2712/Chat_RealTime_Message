package com.example.realtime_message_application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.realtime_message_application.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Cấu hình allowed origins (có thể từ env var hoặc properties)
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            for (String origin : allowedOrigins.split(",")) {
                configuration.addAllowedOrigin(origin.trim());
            }
        } else {
            // Default: cho phép localhost khi dev
            configuration.addAllowedOrigin("http://localhost:3000");
            configuration.addAllowedOrigin("http://localhost:5173");
            configuration.addAllowedOrigin("http://localhost:8080");
        }

        // Allow methods
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("OPTIONS");
        configuration.addAllowedMethod("PATCH");

        // Allow headers
        configuration.addAllowedHeader("*");

        // Allow credentials (cookie, auth header)
        configuration.setAllowCredentials(true);

        // Expose headers
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("Content-Type");

        // Max age
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Cấu hình CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disable CSRF vì dùng JWT (stateless)
                .csrf(csrf -> csrf.disable())

                // Quản lý Session: Không tạo session trên server (stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Cho phép tất cả requests (hoặc bạn có thể bỏ comment để xác thực)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

                // Thêm Filter kiểm tra JWT trước khi xử lý các filter mặc định của Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Security headers cho production
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .xssProtection(xss -> xss.disable())
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")));

        return http.build();
    }
}
