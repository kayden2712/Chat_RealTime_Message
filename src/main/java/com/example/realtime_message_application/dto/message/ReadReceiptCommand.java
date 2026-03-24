package com.example.realtime_message_application.dto.message;

public record ReadReceiptCommand(Long messageId, Long readerId) {

}
