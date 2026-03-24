package com.example.realtime_message_application.dto.message;

import java.time.Instant;

public record ReadReceiptResponse(Long readReceiptId, Long messageId, String readerName, Instant readAt) {

}
