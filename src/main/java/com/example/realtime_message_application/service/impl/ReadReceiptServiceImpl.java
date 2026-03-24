package com.example.realtime_message_application.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.realtime_message_application.dto.message.ReadReceiptCommand;
import com.example.realtime_message_application.dto.message.ReadReceiptDTO;
import com.example.realtime_message_application.dto.message.ReadReceiptResponse;
import com.example.realtime_message_application.model.Message;
import com.example.realtime_message_application.model.ReadReceipt;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.MessageRepository;
import com.example.realtime_message_application.repository.ReadReceiptRepository;
import com.example.realtime_message_application.service.ReadReceiptService;
import com.example.realtime_message_application.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReadReceiptServiceImpl implements ReadReceiptService {

    private final ReadReceiptRepository readReceiptRepo;
    private final UserService userService;
    private final MessageRepository messageRepo;

    public ReadReceiptResponse convertTResponse(ReadReceipt readReceipt) {
        return new ReadReceiptResponse(
                readReceipt.getReadReceiptId(),
                readReceipt.getMessage().getMessageId(),
                readReceipt.getUser().getUsername(),
                readReceipt.getReadAt());
    }

    @Override
    public ReadReceiptResponse markAsRead(ReadReceiptCommand command) {
        Message msg = messageRepo.findById(command.messageId())
                .orElseThrow(() -> new RuntimeException("Message not found"));
        User user = userService.getEntityByUserId(command.readerId());

        ReadReceipt receipt = ReadReceipt.builder()
                .message(msg)
                .user(user)
                .build();

        if (readReceiptRepo.existsByMessageIdAndReaderId(command.messageId(), command.readerId())) {
            throw new RuntimeException("Already read by this user");
        }

        return convertTResponse(readReceiptRepo.save(receipt));
    }

    @Override
    public ReadReceiptResponse markAsReadForActor(ReadReceiptDTO incoming, int actorId) {
        ReadReceiptCommand command = new ReadReceiptCommand(incoming.messageId(), (long) actorId);
        return markAsRead(command);
    }

    @Override
    public List<ReadReceiptResponse> getReadReceipts(Long messageId) {
        if (!messageRepo.existsByMessageId(messageId)) {
            throw new RuntimeException("Message not found");
        }

        List<ReadReceipt> receipts = readReceiptRepo.findAllReadersForMessage(messageId);
        return receipts.stream().map(this::convertTResponse).toList();
    }

}
