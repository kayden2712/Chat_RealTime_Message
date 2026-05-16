package com.example.realtime_message_application.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModerationGroupDTO {
    private Long conversationId;
    private Long targetUserId;
    private Long moderatorId;
    private String action;
    
    // Thêm trường duration nếu cần thiết
    private Integer duration; // Thời gian mute (nếu action là MUTE)

}
