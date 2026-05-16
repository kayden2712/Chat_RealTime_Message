package com.example.realtime_message_application.dto.conversation;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
public class ChatEvent<T> {
    private String message;
    private T data;
}
