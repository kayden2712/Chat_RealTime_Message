package com.example.realtime_message_application.dto.conversation;

public record BanUserDTO(Long conversationId, Long targetUserId, Long adminId, String reason) {
    
}
