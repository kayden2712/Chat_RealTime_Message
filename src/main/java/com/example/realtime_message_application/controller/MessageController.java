package com.example.realtime_message_application.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.realtime_message_application.component.ChatEventPublisher;
import com.example.realtime_message_application.dto.conversation.ChatEvent;
import com.example.realtime_message_application.dto.message.ChatMessage;
import com.example.realtime_message_application.dto.message.DeleteMessage;
import com.example.realtime_message_application.dto.message.EditMessage;
import com.example.realtime_message_application.dto.message.MessageResponse;
import com.example.realtime_message_application.dto.message.PinMessage;
import com.example.realtime_message_application.model.Message;
import com.example.realtime_message_application.service.MessageService;
import com.example.realtime_message_application.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ChatEventPublisher chatEventPublisher;
    private final NotificationService notificationService;

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

    @PostMapping("/send-file")
    public ResponseEntity<MessageResponse> sendMessage(@ModelAttribute ChatMessage chatMessage) {
        if (chatMessage.getFile() == null || chatMessage.getFile().isEmpty()) {
            throw new RuntimeException(
                    "This endpoint is only for file/image attachments. Please use WebSocket for text messages.");
        }
        MessageResponse savedMessage = messageService.sendMessageForActor(chatMessage, chatMessage.getSenderId());

        chatEventPublisher.broadcastToConversation(
                chatMessage.getConversationId(),
                chatMessage.getSenderId(),
                new ChatEvent<>("NEW_MESSAGE", savedMessage));
        notificationService.notifyParticipants(chatMessage.getConversationId(), savedMessage);
        return ResponseEntity.ok(savedMessage);
    }

    @PostMapping("/edit")
    public ResponseEntity<MessageResponse> editMessage(@RequestBody EditMessage editMessage,
            @RequestParam Long actorId) {
        MessageResponse updatedMessage = messageService.editMessageForActor(editMessage, actorId);

        chatEventPublisher.broadcastToConversation(
                editMessage.conversationId(),
                actorId,
                new ChatEvent<>("MESSAGE_EDITED", updatedMessage));

        return ResponseEntity.ok(updatedMessage);
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteMessage(@RequestBody DeleteMessage deleteMessage, @RequestParam Long actorId) {
        messageService.deleteMessageForActor(deleteMessage, actorId);
        MessageResponse deletedMessage = messageService.getMessageResponseById(deleteMessage.messageId());

        chatEventPublisher.broadcastToConversation(
                deleteMessage.conversationId(),
                actorId,
                new ChatEvent<>("MESSAGE_DELETED", deletedMessage));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete-permanently")
    public ResponseEntity<Void> deleteMessagePermanently(@RequestBody DeleteMessage deleteMessage,
            @RequestParam Long actorId) { // SỬA LỖI: Chuyển từ @PathVariable sang @RequestParam cho khớp Endpoint URL
        messageService.deleteMessagePermanentlyForActor(deleteMessage, actorId);
        MessageResponse removedMessage = messageService.getMessageResponseById(deleteMessage.messageId());

        chatEventPublisher.broadcastToConversation(
                deleteMessage.conversationId(),
                actorId,
                new ChatEvent<>("MESSAGE_HARD_DELETED", removedMessage));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pin")
    public ResponseEntity<MessageResponse> pinMessage(@RequestBody PinMessage pinMessage) {
        MessageResponse pinnedMessage = messageService.pinMessageForActor(pinMessage, pinMessage.senderId());

        chatEventPublisher.broadcastToConversation(
                pinMessage.conversationId(),
                pinMessage.senderId(),
                new ChatEvent<>("MESSAGE_PINNED", pinnedMessage));

        notificationService.notifyParticipants(pinMessage.conversationId(), pinnedMessage);

        return ResponseEntity.ok(pinnedMessage);
    }

    @PostMapping("/unpin")
    public ResponseEntity<MessageResponse> unpinMessage(@RequestBody PinMessage unpinMessage) {
        MessageResponse unpinnedMessage = messageService.unpinMessageForActor(unpinMessage, unpinMessage.senderId());

        chatEventPublisher.broadcastToConversation(
                unpinMessage.conversationId(),
                unpinMessage.senderId(),
                new ChatEvent<>("MESSAGE_UNPINNED", unpinnedMessage));
        // Sếp yêu cầu PIN thì thông báo, nên UNPIN cũng gửi notification để thiết bị
        // cập nhật trạng thái mất ghim nhé!
        notificationService.notifyParticipants(unpinMessage.conversationId(), unpinnedMessage);
        return ResponseEntity.ok(unpinnedMessage);
    }
}