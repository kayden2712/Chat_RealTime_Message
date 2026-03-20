package com.example.realtime_message_application.dto.conversation;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateConvImage {
    Long conversationId;
    
    MultipartFile image;
}
