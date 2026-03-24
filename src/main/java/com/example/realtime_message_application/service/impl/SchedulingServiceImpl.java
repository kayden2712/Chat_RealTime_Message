package com.example.realtime_message_application.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.realtime_message_application.model.Message;
import com.example.realtime_message_application.repository.MessageRepository;
import com.example.realtime_message_application.service.SchedulingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchedulingServiceImpl implements SchedulingService {

    private final MessageRepository messageRepository;

    @Override
    @Scheduled(fixedRate = 60 * 60 * 1000) // min x sec x milli = 1 hour
    public void deleteExpiredMessages() {
        LocalDateTime now = LocalDateTime.now();
        List<Message> expired = messageRepository.findExpiredMessages(now);

        if (!expired.isEmpty()) {
            expired.forEach(messageRepository::delete);
        }

    }

}
