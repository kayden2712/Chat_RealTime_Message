package com.example.realtime_message_application.dto.conversation;

import com.example.realtime_message_application.enums.ParticipantRole;

public record AddParticipant(Long conversationId, Long userId, ParticipantRole role, Long adminId) {

    public AddParticipant(long l, long m, long n, ParticipantRole member) {
        this(l, m, member, n);
    }

}
