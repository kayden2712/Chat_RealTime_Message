package com.example.realtime_message_application.dto.conversation;

import java.time.Instant;

public record ParticipantResponse(Long cpId, Long userId, Long convId, String nickname, String role, Instant joinedOn) {

}
