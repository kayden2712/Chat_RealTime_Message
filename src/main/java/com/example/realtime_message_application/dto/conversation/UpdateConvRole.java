package com.example.realtime_message_application.dto.conversation;

import com.example.realtime_message_application.enums.ParticipantRole;

public record UpdateConvRole(Long conversationId, Long userId, ParticipantRole role, Long adminId) {

}
