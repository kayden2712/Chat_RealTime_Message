package com.example.realtime_message_application.dto.user;

import java.time.Instant;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserDTO {

    @NotNull(message = "Username is required")
    private String username;

    @NotNull(message = "Phone number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Invalid phone number")
    private String phoneNo;

    @NotNull(message = "Password is required")
    private String password;

    @NotNull(message = "Email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    private String email;

    private String nickname;
    private MultipartFile imageFile;
    private String bio;

    private boolean online;
    private Instant lastSeen;

}

