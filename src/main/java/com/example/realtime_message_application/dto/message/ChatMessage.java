package com.example.realtime_message_application.dto.message;

import org.springframework.web.multipart.MultipartFile;

import com.example.realtime_message_application.enums.MessageType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatMessage {
    private Long conversationId;
    private Long senderId;
    private String content;
    private MessageType type;

    private MultipartFile file;

    private Long replyToMessageId;
}
