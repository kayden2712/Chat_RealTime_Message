package com.example.realtime_message_application.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.realtime_message_application.component.ChatEventPublisher;
import com.example.realtime_message_application.dto.conversation.AddParticipant;
import com.example.realtime_message_application.dto.conversation.ArchiveConv;
import com.example.realtime_message_application.dto.conversation.BlockingDTO;
import com.example.realtime_message_application.dto.conversation.ChatEvent;
import com.example.realtime_message_application.dto.conversation.ConversationResponse;
import com.example.realtime_message_application.dto.conversation.FavoriteConv;
import com.example.realtime_message_application.dto.conversation.LeaveConversation;
import com.example.realtime_message_application.dto.conversation.ModerationGroupDTO;
import com.example.realtime_message_application.dto.conversation.MuteConv;
import com.example.realtime_message_application.dto.conversation.RemoveParticipant;
import com.example.realtime_message_application.dto.conversation.UnMuteConv;
import com.example.realtime_message_application.dto.conversation.UpdateConvDescription;
import com.example.realtime_message_application.dto.conversation.UpdateConvImage;
import com.example.realtime_message_application.dto.conversation.UpdateConvRole;
import com.example.realtime_message_application.dto.conversation.UpdateConvTitle;
import com.example.realtime_message_application.dto.message.ChatMessage;
import com.example.realtime_message_application.dto.message.DeleteMessage;
import com.example.realtime_message_application.dto.message.EditMessage;
import com.example.realtime_message_application.dto.message.MessageResponse;
import com.example.realtime_message_application.dto.message.PinMessage;
import com.example.realtime_message_application.dto.message.ReadReceiptDTO;
import com.example.realtime_message_application.model.Message;
import com.example.realtime_message_application.security.SecurityUtils;
import com.example.realtime_message_application.service.BlockedService;
import com.example.realtime_message_application.service.ConversationService;
import com.example.realtime_message_application.service.MessageService;
import com.example.realtime_message_application.service.NotificationService;
import com.example.realtime_message_application.service.ReadReceiptService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {

        private final SimpMessagingTemplate messagingTemplate;
        private final MessageService messageService;
        private final ConversationService conversationService;
        private final BlockedService blockedService;
        private final ReadReceiptService readReceiptService;
        private final NotificationService notificationService;
        private final ChatEventPublisher chatEventPublisher;

        @MessageMapping("chat.send")
        public void processGroupMessage(@Payload ChatMessage chatMessage) {
                long senderId = SecurityUtils.getUserId();
                MessageResponse savedMessage = messageService.sendMessageForActor(chatMessage, senderId);

                chatEventPublisher.broadcastToConversation(
                                chatMessage.getConversationId(),
                                senderId,
                                new ChatEvent<>("MESSAGE_SENT", savedMessage));
                notificationService.notifyParticipants(chatMessage.getConversationId(), savedMessage);
        }

        @MessageMapping("chat.edit")
        public void editMessage(@Payload EditMessage editMessage) {
                long userId = SecurityUtils.getUserId();
                MessageResponse updatedMessage = messageService.editMessageForActor(editMessage, userId);
                chatEventPublisher.broadcastToConversation(
                                editMessage.conversationId(),
                                userId,
                                new ChatEvent<>("MESSAGE_EDITED", updatedMessage));
                // notificationService.notifyParticipants(editMessage.conversationId(),
                // updatedMessage);
        }

        @MessageMapping("chat.delete")
        public void deleteMessage(@Payload DeleteMessage deleteMessage) {
                long userId = SecurityUtils.getUserId();
                messageService.deleteMessageForActor(deleteMessage, userId);
                MessageResponse removedMessage = messageService.getMessageResponseById(deleteMessage.messageId());
                chatEventPublisher.broadcastToConversation(
                                deleteMessage.conversationId(),
                                userId,
                                new ChatEvent<>("MESSAGE_DELETED", removedMessage));
        }

        @MessageMapping("chat.hardDelete")
        public void hardDeleteMessage(@Payload DeleteMessage deleteMessage) {
                long userId = SecurityUtils.getUserId();
                messageService.deleteMessagePermanentlyForActor(deleteMessage, userId);
                MessageResponse removedMessage = messageService.getMessageResponseById(deleteMessage.messageId());
                chatEventPublisher.broadcastToConversation(
                                deleteMessage.conversationId(),
                                userId,
                                new ChatEvent<>("MESSAGE_HARD_DELETED", removedMessage));
        }

        @MessageMapping("chat.pin")
        public void pinMessage(@Payload PinMessage pinMessage) {
                long userId = SecurityUtils.getUserId();
                messageService.pinMessageForActor(pinMessage, userId);
                MessageResponse pinnedMessage = messageService.getMessageResponseById(pinMessage.messageId());
                chatEventPublisher.broadcastToConversation(
                                pinMessage.conversationId(),
                                userId,
                                new ChatEvent<>("MESSAGE_PINNED", pinnedMessage));
                notificationService.notifyParticipants(pinMessage.conversationId(), pinnedMessage);
        }

        @MessageMapping("chat.unpin")
        public void unpinMessage(@Payload PinMessage pinMessage) {
                long userId = SecurityUtils.getUserId();
                messageService.unpinMessageForActor(pinMessage, userId);
                MessageResponse unpinnedMessage = messageService.getMessageResponseById(pinMessage.messageId());
                chatEventPublisher.broadcastToConversation(
                                pinMessage.conversationId(),
                                userId,
                                new ChatEvent<>("MESSAGE_UNPINNED", unpinnedMessage));
        }

        @MessageMapping("chat.updateAvatar")
        public void updateConversationAvatar(@Payload UpdateConvImage avatarUpdate) {
                long userId = SecurityUtils.getUserId();
                avatarUpdate.setUserId(userId);
                conversationService.updateConversationImage(avatarUpdate);

                ConversationResponse updatedConversation = conversationService
                                .getConversationById(avatarUpdate.getConversationId());
                chatEventPublisher.broadcastToConversation(
                                avatarUpdate.getConversationId(),
                                userId,
                                new ChatEvent<>("AVATAR_UPDATED", updatedConversation));
        }

        @MessageMapping("chat.addParticipant")
        public void addParticipant(@Payload AddParticipant participant) {
                long userId = SecurityUtils.getUserId();
                conversationService.AddParticipantInConversation(participant);

                ConversationResponse updatedConversation = conversationService
                                .getConversationById(participant.conversationId());
                chatEventPublisher.broadcastToConversation(
                                participant.conversationId(),
                                userId,
                                new ChatEvent<>("PARTICIPANT_ADDED", updatedConversation));
        }

        @MessageMapping("chat.removeParticipant")
        public void removeParticipant(@Payload RemoveParticipant participant) {
                long userId = SecurityUtils.getUserId();
                conversationService.removeParticipantInConversation(participant);

                ConversationResponse updatedConversation = conversationService
                                .getConversationById(participant.conversationId());
                chatEventPublisher.broadcastToConversation(
                                participant.conversationId(),
                                userId,
                                new ChatEvent<>("PARTICIPANT_REMOVED", updatedConversation));
        }

        @MessageMapping("chat.leave")
        public void leaveConversation(@Payload LeaveConversation participant) {
                Long userId = SecurityUtils.getUserId();
                conversationService.leaveConversation(new LeaveConversation(participant.conversationId(), userId));
                ConversationResponse updatedConversation = conversationService
                                .getConversationById(participant.conversationId());
                chatEventPublisher.broadcastToConversation(
                                participant.conversationId(),
                                userId,
                                new ChatEvent<>("CONVERSATION_LEFT", updatedConversation));
        }

        @MessageMapping("chat.renameTitle")
        public void renameConversationTitle(@Payload UpdateConvTitle titleUpdate) {
                long userId = SecurityUtils.getUserId();
                conversationService.updateConversationTitle(titleUpdate);
                ConversationResponse updatedConversation = conversationService
                                .getConversationById(titleUpdate.conversationId());
                chatEventPublisher.broadcastToConversation(
                                titleUpdate.conversationId(),
                                userId,
                                new ChatEvent<>("CONVERSATION_TITLE_RENAMED", updatedConversation));
        }

        @MessageMapping("chat.description")
        public void updateConversationDescription(@Payload UpdateConvDescription descriptionUpdate) {
                long userId = SecurityUtils.getUserId();
                conversationService.updateConversationDescription(descriptionUpdate);
                ConversationResponse updatedConversation = conversationService
                                .getConversationById(descriptionUpdate.conversationId());
                chatEventPublisher.broadcastToConversation(
                                descriptionUpdate.conversationId(),
                                userId,
                                new ChatEvent<>("CONVERSATION_DESCRIPTION_UPDATED", updatedConversation));
        }

        @MessageMapping("chat.changeRole")
        public void changeParticipantRole(@Payload UpdateConvRole roleUpdate) {
                long userId = SecurityUtils.getUserId();
                conversationService.updateConversationRole(roleUpdate);
                ConversationResponse updatedConversation = conversationService
                                .getConversationById(roleUpdate.conversationId());
                chatEventPublisher.broadcastToConversation(
                                roleUpdate.conversationId(),
                                userId,
                                new ChatEvent<>("PARTICIPANT_ROLE_CHANGED", updatedConversation));
        }

        @MessageMapping("chat.mute")
        public void muteConversation(@Payload MuteConv muteRequest) {
                long userId = SecurityUtils.getUserId();
                conversationService.muteConversation(muteRequest);
                ConversationResponse updatedConversation = conversationService
                                .getConversationById(muteRequest.conversationId());
                chatEventPublisher.broadcastToConversation(
                                muteRequest.conversationId(),
                                userId,
                                new ChatEvent<>("CONVERSATION_MUTED", updatedConversation));
        }

        @MessageMapping("chat.unmute")
        public void unmuteConversation(@Payload UnMuteConv unmuteRequest) {
                long userId = SecurityUtils.getUserId();
                conversationService.unMuteConversation(unmuteRequest);
                ConversationResponse updatedConversation = conversationService
                                .getConversationById(unmuteRequest.conversationId());
                chatEventPublisher.broadcastToConversation(
                                unmuteRequest.conversationId(),
                                userId,
                                new ChatEvent<>("CONVERSATION_UNMUTED", updatedConversation));
        }

        @MessageMapping("chat.favorite")
        public void favoriteConversation(@Payload FavoriteConv favoriteRequest) {
                long userId = SecurityUtils.getUserId();
                conversationService.addOrRemoveAsFavorites(favoriteRequest);
                ConversationResponse updatedConversation = conversationService
                                .getConversationById(favoriteRequest.conversationId());
                chatEventPublisher.broadcastToConversation(
                                favoriteRequest.conversationId(),
                                userId,
                                new ChatEvent<>("CONVERSATION_FAVORITED", updatedConversation));
        }

        @MessageMapping("chat.archive")
        public void archiveConversation(@Payload ArchiveConv archiveRequest) {
                long userId = SecurityUtils.getUserId();
                conversationService.addOrRemoveAsArchive(archiveRequest);
                ConversationResponse updatedConversation = conversationService
                                .getConversationById(archiveRequest.conversationId());
                chatEventPublisher.broadcastToConversation(
                                archiveRequest.conversationId(),
                                userId,
                                new ChatEvent<>("CONVERSATION_ARCHIVED", updatedConversation));
        }

        @MessageMapping("chat.block")
        public void blockUser(@Payload BlockingDTO blockRequest) {
                long blockerId = SecurityUtils.getUserId();
                blockedService.blockUser(blockRequest);

                messagingTemplate.convertAndSendToUser(
                                String.valueOf(blockerId),
                                "/queue/block",
                                "Blocked user Id: " + blockRequest.blockedId());
        }

        @MessageMapping("chat.unblock")
        public void unblockUser(@Payload BlockingDTO unblockRequest) {
                long blockerId = SecurityUtils.getUserId();
                blockedService.unblockUser(unblockRequest);

                messagingTemplate.convertAndSendToUser(
                                String.valueOf(blockerId),
                                "/queue/unblock",
                                "Unblocked user Id: " + unblockRequest.blockedId());
        }

        @MessageMapping("chat.read")
        public void markAsRead(@Payload ReadReceiptDTO readReceipt) {
                Message msg = messageService.getMessageByIdWithDetails(readReceipt.messageId());
                readReceiptService.markAsRead(readReceipt);

                long senderId = SecurityUtils.getUserId();
                long conversationId = msg.getConversation().getConversationId();

                chatEventPublisher.broadcastToConversation(
                                conversationId,
                                senderId,
                                new ChatEvent<>("MESSAGE_READ", readReceipt));
        }

        @MessageMapping("chat.moderate")
        public void moderateContent(@Payload ModerationGroupDTO moderationDTO) {
                Long moderatorId = SecurityUtils.getUserId();
                moderationDTO.setModeratorId(moderatorId);
                conversationService.moderateGroup(moderationDTO);

                chatEventPublisher.broadcastToConversation(
                                moderationDTO.getConversationId(),
                                moderatorId,
                                new ChatEvent<>("GROUP_MODERATED", moderationDTO));
        }
}
