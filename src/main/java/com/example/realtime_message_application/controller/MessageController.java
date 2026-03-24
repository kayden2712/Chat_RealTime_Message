package com.example.realtime_message_application.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.realtime_message_application.dto.message.ChatMessage;
import com.example.realtime_message_application.dto.message.DeleteMessage;
import com.example.realtime_message_application.dto.message.EditMessage;
import com.example.realtime_message_application.dto.message.MessageResponse;
import com.example.realtime_message_application.dto.message.PinMessage;
import com.example.realtime_message_application.dto.message.RestoreMessage;
import com.example.realtime_message_application.model.Message;
import com.example.realtime_message_application.service.MessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/dev/{messageId}")
    public ResponseEntity<Message> checkGetMessageById(@PathVariable Long messageId) {
        return ResponseEntity.ok(messageService.getMessageById(messageId));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long messageId) {
        return ResponseEntity.ok(messageService.getMessageByIdWithDetails(messageId));
    }

    @GetMapping("/response/{messageId}")
    public ResponseEntity<MessageResponse> getMessageResponseById(@PathVariable Long messageId) {
        return ResponseEntity.ok(messageService.getMessageResponseById(messageId));
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageResponse>> getAllMessagesInCOnv(@PathVariable Long conversationId) {
        return ResponseEntity.ok(messageService.getAllMessageInConversation(conversationId));
    }

    @GetMapping("/conversation/{conversationId}/page")
    public ResponseEntity<Page<MessageResponse>> getMessages(@PathVariable Long conversationId,
            @PathVariable Long requestId, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(messageService.getMessages(conversationId, requestId, pageable));
    }

    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(@ModelAttribute ChatMessage chatMessage) {
        return ResponseEntity.ok(messageService.sendMessageForActor(chatMessage, chatMessage.getSenderId()));
    }

    @PostMapping("/edit")
    public ResponseEntity<MessageResponse> editMessage(@RequestBody EditMessage editMessage, @PathVariable Long actorId) {
        return ResponseEntity.ok(messageService.editMessageForActor(editMessage, actorId));
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteMessage(@RequestBody DeleteMessage deleteMessage, @PathVariable Long actorId) {
        messageService.deleteMessageForActor(deleteMessage, actorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete-permanently")
    public ResponseEntity<Void> deleteMessagePermanently(@RequestBody DeleteMessage deleteMessage,
            @PathVariable Long actorId) {
        messageService.deleteMessagePermanentlyForActor(deleteMessage, actorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restore")
    public ResponseEntity<MessageResponse> restoreMessage(@RequestBody RestoreMessage restoreMessage) {
        return ResponseEntity.ok(messageService.restoreMessage(restoreMessage));
    }

    @PostMapping("/pin")
    public ResponseEntity<MessageResponse> pinMessage(@RequestBody PinMessage pinMessage) {
        return ResponseEntity.ok(messageService.pinMessageForActor(pinMessage, pinMessage.senderId()));
    }

    @PostMapping("/unpin")
    public ResponseEntity<MessageResponse> unpinMessage(@RequestBody PinMessage unpinMessage) {
        return ResponseEntity.ok(messageService.unpinMessageForActor(unpinMessage, unpinMessage.senderId()));
    }
}
