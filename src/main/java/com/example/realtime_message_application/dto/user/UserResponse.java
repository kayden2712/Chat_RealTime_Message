package com.example.realtime_message_application.dto.user;

import java.time.Instant;

public record UserResponse(
        Long userId,
        String username,
        String phoneNo,
        String nickname,
        Instant createOn,
        String bio,
        String online,
        String lastSeen) {

}
