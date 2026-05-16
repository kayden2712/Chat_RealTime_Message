package com.example.realtime_message_application.dto.message;

import com.example.realtime_message_application.enums.ConversationType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisChatEvent {
    private  Long conversationId;
    private  Long senderId;
    private  ConversationType conversationType;
    private  String payloadJson;
}
