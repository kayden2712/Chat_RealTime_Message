package com.example.realtime_message_application.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.realtime_message_application.dto.message.ChatMessage;
import com.example.realtime_message_application.dto.message.DeleteMessage;
import com.example.realtime_message_application.dto.message.EditMessage;
import com.example.realtime_message_application.dto.message.MessageCommands.DeleteMessageCommand;
import com.example.realtime_message_application.dto.message.MessageCommands.EditMessageCommand;
import com.example.realtime_message_application.dto.message.MessageCommands.PinMessageCommand;
import com.example.realtime_message_application.dto.message.MessageCommands.SendMessageCommand;
import com.example.realtime_message_application.dto.message.MessageResponse;
import com.example.realtime_message_application.dto.message.PinMessage;
import com.example.realtime_message_application.dto.message.RestoreMessage;
import com.example.realtime_message_application.enums.ConversationType;
import com.example.realtime_message_application.enums.MessageType;
import com.example.realtime_message_application.mapper.MessageMapper;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.model.ConversationParticipant;
import com.example.realtime_message_application.model.Message;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.BanRepository;
import com.example.realtime_message_application.repository.BlockRepository;
import com.example.realtime_message_application.repository.ConversationRepository;
import com.example.realtime_message_application.repository.MessageRepository;
import com.example.realtime_message_application.service.ConversationService;
import com.example.realtime_message_application.service.MessageService;
import com.example.realtime_message_application.service.ParticipantService;
import com.example.realtime_message_application.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationService conversationService;
    private final UserService userService;
    private final MessageMapper messageMapper;
    private final BanRepository banRepository;
    private final BlockRepository blockRepository;
    private final ParticipantService participantService;

    @Override
    public Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
    }

    @Override
    public MessageResponse getMessageResponseById(Long messageId) {
        Message message = getMessageById(messageId);
        return messageMapper.toMessageResponse(message);
    }

    @Override
    public Message getMessageByIdWithDetails(Long messageId) {
        Message message = messageRepository.findByMessageIdWithDetails(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setReplies(new HashSet<>(message.getReplies())); // ép Hibernate phải tải dữ liệu từ database ngay lập
                                                                 // tức
        return message;
    }

    @Override
    public List<MessageResponse> getReplies(Long messageId) {
        List<Message> messages = messageRepository.findMessageReplies(messageId);
        return messages.stream().map(messageMapper::toMessageResponse).toList();
    }

    @Override
    public List<MessageResponse> getAllMessageInConversation(Long conversationId) {
        validateCOnversation(conversationId);

        List<Message> messages = messageRepository.findAllMessagesByConversationId(conversationId);
        return messages.stream().map(messageMapper::toMessageResponse).toList();
    }

    @Override
    public Page<MessageResponse> getMessages(Long conversationId, Long requestId, Pageable pageable) {

        validateCOnversation(conversationId);
        validateUser(requestId);
        validateParticipant(conversationId, requestId);

        Page<Message> messages = messageRepository.findByConversationId(conversationId, pageable);
        return messages.map(messageMapper::toMessageResponse);
    }

    @Override
    public MessageResponse restoreMessage(RestoreMessage restoreMessage) {
        Message deleted = getMessageById(restoreMessage.messageId());

        validateUser(restoreMessage.userId());

        if (!deleted.getSender().getUserId().equals(restoreMessage.userId())) {
            throw new RuntimeException("You are not the sender of this message");
        }

        deleted.setDeleted(false);
        deleted.setDeletedBy(null);

        return messageMapper.toMessageResponse(deleted);

    }

    @Override
    public void deleteMessage(DeleteMessageCommand command) {
        Message message = getMessageByIdWithDetails(command.messageId());

        validateUser(command.actorId());
        validateParticipant(message.getConversation().getConversationId(), command.actorId());

        if (!message.getSender().getUserId().equals(command.actorId())) {
            throw new RuntimeException("You are not the sender of this message");
        }

        message.setDeletedBy(message.getSender().getUsername());
        messageRepository.delete(message);
    }

    @Override
    public void deleteMessageForActor(DeleteMessage incoming, Long actorId) {
        DeleteMessageCommand command = new DeleteMessageCommand(incoming.conversationId(), incoming.messageId(),
                actorId);
        deleteMessage(command);
    }

    @Override
    public void deleteMessagePermanently(DeleteMessageCommand command) {
        Message message = getMessageById(command.messageId());
        validateUser(command.actorId());

        if (!message.getSender().getUserId().equals(command.actorId())) {
            throw new RuntimeException("You are not the sender of this message");
        }

        messageRepository.permanentDeletionOfMessage(command.messageId());
    }

    @Override
    public void deleteMessagePermanentlyForActor(DeleteMessage incoming, Long actorId) {
        DeleteMessageCommand command = new DeleteMessageCommand(incoming.conversationId(), incoming.messageId(),
                actorId);
        deleteMessagePermanently(command);
    }

    @Override
    public void deleteAllInConversation(Long conversationId) {
        conversationService.removeAllMessageInConversation(conversationId);

    }

    @Override
    public MessageResponse pinMessage(PinMessageCommand pinMessage) {

        validateCOnversation(pinMessage.conversationId());
        validateUser(pinMessage.actorId());
        validateBan(pinMessage.conversationId(), pinMessage.actorId());
        validateParticipant(pinMessage.conversationId(), pinMessage.actorId());

        Message message = getMessageById(pinMessage.messageId());

        if (conversationRepository.countNoOfPinnedMessageConv(pinMessage.conversationId()) > 10) {
            throw new RuntimeException("Maximum number of pinned messages reached");
        }

        if (message.isPinned()) {
            throw new RuntimeException("Message is already pinned");
        }

        message.setPinned(true);
        message.setPinnedBy(message.getSender().getUsername());
        return messageMapper.toMessageResponse(message);
    }

    @Override
    public MessageResponse pinMessageForActor(PinMessage incoming, Long actorId) {
        PinMessageCommand command = new PinMessageCommand(incoming.messageId(), incoming.conversationId(), actorId);
        return pinMessage(command);
    }

    @Override
    public MessageResponse unpinMessage(PinMessageCommand pinMessage) {

        validateCOnversation(pinMessage.conversationId());
        validateUser(pinMessage.actorId());
        validateBan(pinMessage.conversationId(), pinMessage.actorId());
        validateParticipant(pinMessage.conversationId(), pinMessage.actorId());

        Message message = getMessageById(pinMessage.messageId());

        if (!message.isPinned()) {
            throw new RuntimeException("Message is not pinned");
        }
        message.setPinned(false);
        message.setPinnedBy(null);
        return messageMapper.toMessageResponse(message);
    }

    @Override
    public MessageResponse unpinMessageForActor(PinMessage incoming, Long actorId) {
        PinMessageCommand command = new PinMessageCommand(incoming.messageId(), incoming.conversationId(), actorId);
        return unpinMessage(command);
    }

    @Override
    public MessageResponse editMessage(EditMessageCommand editMessage) {

        validateCOnversation(editMessage.conversationId());
        validateUser(editMessage.senderId());
        validateBan(editMessage.conversationId(), editMessage.senderId());
        validateParticipant(editMessage.conversationId(), editMessage.senderId());

        Message message = getMessageByIdWithDetails(editMessage.messageId());

        if (!message.getSender().getUserId().equals(editMessage.senderId())) {
            throw new RuntimeException("You are not the sender of this message");
        }

        if (message.getMessageType() != MessageType.TEXT) {
            throw new RuntimeException("Only Text messages can be edited.");
        }

        if (!message.getConversation().getConversationId().equals(editMessage.conversationId())) {
            throw new RuntimeException("Message is not in the conversation");
        }

        message.setContent(editMessage.newContent());
        message.setEditedAt(Instant.now());
        return messageMapper.toMessageResponse(message);
    }

    @Override
    public MessageResponse editMessageForActor(EditMessage incoming, Long actorId) {
        EditMessageCommand command = new EditMessageCommand(incoming.messageId(), incoming.conversationId(), actorId,
                incoming.content());
        return editMessage(command);
    }

    @Override
    public MessageResponse sendMessage(SendMessageCommand send) {
        Conversation conversation = conversationService.getEntityByConvId(send.conversationId());
        User sender = userService.getEntityByUserId(send.senderId());

        validateParticipant(send.conversationId(), send.senderId());

        // check block (only for private conversation)
        if (conversation.getType() == ConversationType.PRIVATE) {
            User otherUser = conversation.getParticipants().stream().map(ConversationParticipant::getUser)
                    .filter(u -> !u.getUserId().equals(send.senderId())).findFirst()
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (blockRepository.existsByBlockerAndBlocked(otherUser.getUserId(), send.senderId())) {
                throw new RuntimeException("You are blocked by this user");
            }
            if (blockRepository.existsByBlockerAndBlocked(send.senderId(), otherUser.getUserId())) {
                throw new RuntimeException("You have blocked this user");
            }
        }

        if (banRepository.existsByConvIdAndUserId(conversation.getConversationId(), send.senderId())) {
            throw new RuntimeException("You are banned from this conversation");
        }
        ;

        String content = (send.file() != null && !send.file().isEmpty()) ? handleFileUpload(send.file())
                : send.content();

        Message repliesMessage = (send.replyToMessageId() != null) ? getMessageById(send.replyToMessageId())
                : null;

        LocalDateTime expiredDate = (Boolean.TRUE
                .equals(conversation.isDisappearing() && conversation.getExpiryInDays() > 0))
                        ? LocalDateTime.now().plusDays(conversation.getExpiryInDays())
                        : null;

        Message msg = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .messageType(send.messageType())
                .replyTo(repliesMessage)
                .expiresAt(expiredDate)
                .build();

        Message savedMessage = messageRepository.save(msg);
        return messageMapper.toMessageResponse(savedMessage);

    }

    @Override
    public MessageResponse sendMessageForActor(ChatMessage chatMessage, Long actorId) {
        SendMessageCommand command = new SendMessageCommand(
                chatMessage.getConversationId(),
                actorId,
                chatMessage.getContent(),
                chatMessage.getType(),
                chatMessage.getFile(),
                chatMessage.getReplyToMessageId());
        return sendMessage(command);
    }

    private void validateCOnversation(Long convId) {
        if (!conversationRepository.existsById(convId)) {
            throw new RuntimeException("Conversation not found");
        }
    }

    private void validateBan(Long convId, Long userId) {
        if (!banRepository.existsByConvIdAndUserId(convId, userId)) {
            throw new RuntimeException("You are banned from this conversation.");
        }
    }

    private void validateUser(Long userId) {
        if (!userService.isExists(userId)) {
            throw new RuntimeException("User not found");
        }
    }

    private void validateParticipant(Long convId, Long userId) {
        if (!participantService.isExists(convId, userId)) {
            throw new RuntimeException("You are not a participant of this conversation.");
        }
    }

    private String handleFileUpload(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 1. Định nghĩa thư mục lưu trữ (Tương đối so với project root)
            String uploadDir = "attachments";
            Path rootLocation = Paths.get(uploadDir);

            // 2. Tạo thư mục nếu chưa tồn tại (Dùng Files.createDirectories cực kỳ an toàn)
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }

            // 3. Tạo tên file duy nhất: [Timestamp]_[UUID]_[OriginalName]
            // UUID giúp đảm bảo dù hàng nghìn người cùng upload 1 lúc cũng không trùng tên
            String originalFileName = file.getOriginalFilename();
            String cleanFileName = (originalFileName != null) ? originalFileName.replaceAll("\\s+", "_") : "unnamed";
            String uniqueFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8)
                    + "_" + cleanFileName;

            // 4. Resolve đường dẫn an toàn (Ngăn chặn Path Traversal Attack)
            Path destinationFile = rootLocation.resolve(Paths.get(uniqueFileName))
                    .normalize().toAbsolutePath();

            // 5. Lưu file bằng InputStream (Tiết kiệm RAM hơn Files.write)
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // 6. Trả về TÊN FILE để lưu vào Database (Không lưu đường dẫn tuyệt đối)
            return uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi hệ thống khi lưu trữ file: " + e.getMessage(), e);
        }
    }
}