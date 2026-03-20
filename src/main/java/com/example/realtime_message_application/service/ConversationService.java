package com.example.realtime_message_application.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.realtime_message_application.dto.conversation.AddParticipant;
import com.example.realtime_message_application.dto.conversation.ArchiveConv;
import com.example.realtime_message_application.dto.conversation.ConversationDTO;
import com.example.realtime_message_application.dto.conversation.ConversationResponse;
import com.example.realtime_message_application.dto.conversation.FavoriteConv;
import com.example.realtime_message_application.dto.conversation.LeaveConversation;
import com.example.realtime_message_application.dto.conversation.MuteConv;
import com.example.realtime_message_application.dto.conversation.RemoveParticipant;
import com.example.realtime_message_application.dto.conversation.UpdateConvRole;
import com.example.realtime_message_application.dto.conversation.UpdateConvDescription;
import com.example.realtime_message_application.dto.conversation.UpdateConvImage;
import com.example.realtime_message_application.dto.conversation.UpdateConvTitle;
import com.example.realtime_message_application.model.Conversation;

public interface ConversationService {

    List<ConversationResponse> getAllConversations();

    List<ConversationResponse> getAllConversationByUserId(Long userId);

    List<ConversationResponse> getConversationByTitle(String title);

    ConversationResponse getConversationById(Long conversationId);

    ConversationResponse createConversation(ConversationDTO conversation, MultipartFile image);

    List<Long> getAllMessagesByConversationId(Long conversationId);

    void removeAllConversationByUserId(Long userId);

    void removeConversationById(Long conversationId);

    void removeAllMessageInConversation(Long conversationId);

    void removedConversationByTitle(String title);

    ConversationResponse AddParticipantInConversation(AddParticipant addParticipant);

    ConversationResponse removeParticipantInConversation(RemoveParticipant removeParticipant);

    String leaveConversation(LeaveConversation leaveConversation);

    ConversationResponse updateConversationTitle(UpdateConvTitle updateConvTitle);

    ConversationResponse updateConversationImage(UpdateConvImage updateConvImage);

    ConversationResponse updateConversationRole(UpdateConvRole updateConvRole);

    ConversationResponse updateConversationDescription(UpdateConvDescription updateConvDescription);

    void muteConversation(MuteConv muteConv);

    void unMuteConversation(MuteConv unMuteConv);

    void addArchiveConversation(ArchiveConv archiveConv);

    void removeArchiveConversation(ArchiveConv archiveConv);

    void addFavoriteConversation(FavoriteConv favoriteConv);

    void removeFavoriteConversation(FavoriteConv favoriteConv);

    List<ConversationResponse> getAllFavoriteConversation(Long userId);

    List<ConversationResponse> getAllArchivedConversation(Long userId);

    void enableDisappearing(Long convId, int days);

    void disableDisappearing(Long convId);

    ConversationResponse createPrivateGroup(Long userId1, Long userId2);

    Conversation getEntityByConvId(Long convId);

}
