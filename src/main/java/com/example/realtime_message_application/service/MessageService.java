package com.example.realtime_message_application.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.realtime_message_application.dto.message.ChatMessage;
import com.example.realtime_message_application.dto.message.DeleteMessage;
import com.example.realtime_message_application.dto.message.EditMessage;
import com.example.realtime_message_application.dto.message.MessageResponse;
import com.example.realtime_message_application.dto.message.PinMessage;
import com.example.realtime_message_application.dto.message.RestoreMessage;
import com.example.realtime_message_application.dto.message.MessageCommands.DeleteMessageCommand;
import com.example.realtime_message_application.dto.message.MessageCommands.EditMessageCommand;
import com.example.realtime_message_application.dto.message.MessageCommands.PinMessageCommand;
import com.example.realtime_message_application.dto.message.MessageCommands.SendMessageCommand;
import com.example.realtime_message_application.model.Message;

public interface MessageService {
    Message getMessageById(Long messageId);

    MessageResponse getMessageResponseById(Long messageId);

    Message getMessageByIdWithDetails(Long messageId);

    List<MessageResponse> getReplies(Long messageId);

    List<MessageResponse> getAllMessageInConversation(Long conversationId);

    Page<MessageResponse> getMessages(Long conversationId, Long requestId, Pageable pageable);

    MessageResponse restoreMessage(RestoreMessage restoreMessage);

    void deleteMessage(DeleteMessageCommand command);

    void deleteMessageForActor(DeleteMessage incoming, Long actorId);

    void deleteMessagePermanently(DeleteMessageCommand command);

    void deleteMessagePermanentlyForActor(DeleteMessage incoming, Long actorId);

    void deleteAllInConversation(Long conversationId);

    MessageResponse pinMessage(PinMessageCommand pinMessage);

    MessageResponse pinMessageForActor(PinMessage incoming, Long actorId);

    MessageResponse unpinMessage(PinMessageCommand pinMessage);

    MessageResponse unpinMessageForActor(PinMessage incoming, Long actorId);

    MessageResponse editMessage(EditMessageCommand command);

    MessageResponse editMessageForActor(EditMessage incoming, Long actorId);

    MessageResponse sendMessage(SendMessageCommand send);

    MessageResponse sendMessageForActor(ChatMessage chatMessage, Long actorId);

}
