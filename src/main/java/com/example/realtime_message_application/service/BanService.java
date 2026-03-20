package com.example.realtime_message_application.service;

import com.example.realtime_message_application.dto.conversation.BanUserDTO;
import com.example.realtime_message_application.model.BannedUser;

public interface BanService {
    BannedUser findByConversationAndUser(Long conversationId, Long userId);

    void banUser(BanUserDTO banUserDTO);

    void unbanUser(Long conversationId, Long userId, Long adminId);
}
