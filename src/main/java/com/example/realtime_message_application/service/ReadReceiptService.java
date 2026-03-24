package com.example.realtime_message_application.service;

import java.util.List;

import com.example.realtime_message_application.dto.message.ReadReceiptCommand;
import com.example.realtime_message_application.dto.message.ReadReceiptDTO;
import com.example.realtime_message_application.dto.message.ReadReceiptResponse;

public interface ReadReceiptService {

    ReadReceiptResponse markAsRead(ReadReceiptCommand command);

    ReadReceiptResponse markAsReadForActor(ReadReceiptDTO incoming, int actorId);

    List<ReadReceiptResponse> getReadReceipts(Long messageId);
}
