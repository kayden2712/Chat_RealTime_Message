package com.example.realtime_message_application.service;

import java.util.List;

import com.example.realtime_message_application.dto.message.ReadReceiptDTO;
import com.example.realtime_message_application.dto.message.ReadReceiptResponse;

public interface ReadReceiptService {

    ReadReceiptResponse markAsRead(ReadReceiptDTO command);

    List<ReadReceiptResponse> getReadReceipts(Long messageId);
}
