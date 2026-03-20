package com.example.realtime_message_application.dto.conversation;

import java.util.Set;

import com.example.realtime_message_application.enums.ConversationType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {
    private String title;
    private String description;
    
    @NotNull(message = "Conversation must be PRIVATE OR GROUP")
    private ConversationType type;

    @NotNull(message = "Conversation must have a creator")
    private Long creatorId;

    private Set<Long> participantsId;
}
