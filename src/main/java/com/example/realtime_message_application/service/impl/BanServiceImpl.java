package com.example.realtime_message_application.service.impl;

import org.springframework.stereotype.Service;

import com.example.realtime_message_application.dto.conversation.BanUserDTO;
import com.example.realtime_message_application.enums.ParticipantRole;
import com.example.realtime_message_application.exception.ConflictException;
import com.example.realtime_message_application.exception.ForbiddenException;
import com.example.realtime_message_application.exception.ResourceNotFoundException;
import com.example.realtime_message_application.model.BannedUser;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.model.ConversationParticipant;
import com.example.realtime_message_application.repository.BanRepository;
import com.example.realtime_message_application.repository.ConversationRepository;
import com.example.realtime_message_application.repository.ParticipantRepository;
import com.example.realtime_message_application.service.BanService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BanServiceImpl implements BanService {

    private final BanRepository banRepo;
    private final ConversationRepository conversationRepo;
    private final ParticipantRepository participantRepo;

    @Override
    public BannedUser findByConversationAndUser(Long conversationId, Long userId) {
        return banRepo.findByConvIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not banned"));
    }

    @Override
    public void banUser(BanUserDTO banUserDTO) {
        Conversation conversation = conversationRepo.findById(banUserDTO.conversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        ConversationParticipant targetUser = getParticipant(conversation.getConversationId(),
                banUserDTO.targetUserId());
        ConversationParticipant admin = getParticipant(conversation.getConversationId(), banUserDTO.adminId());

        if (admin.getParticipantRole() != ParticipantRole.ADMIN) {
            throw new ForbiddenException("Admins can only ban users.");
        }
        if (targetUser == null) {
            throw new ResourceNotFoundException("User not found.");
        }
        if (banRepo.existsByConvIdAndUserId(banUserDTO.conversationId(), banUserDTO.targetUserId())) {
            throw new ConflictException("User already banned from this conversation.");
        }

        // delete participant
        participantRepo.deleteByConversation_ConversationIdAndUser_UserId(banUserDTO.conversationId(),
                banUserDTO.targetUserId());

        BannedUser bannedUser = BannedUser.builder()
                .conversation(conversation)
                .user(targetUser.getUser())
                .bannedBy(admin.getUser().getUsername())
                .reason(banUserDTO.reason())
                .build();
        banRepo.save(bannedUser);
    }

    @Override
    public void unbanUser(BanUserDTO banUserDTO) {
        BannedUser bannedUser = getBannedUser(banUserDTO.conversationId(), banUserDTO.targetUserId());

        ConversationParticipant admin = getParticipant(banUserDTO.conversationId(), banUserDTO.adminId());
        if (admin.getParticipantRole() != ParticipantRole.ADMIN) {
            throw new ForbiddenException("Admins can only unban users.");
        }
        banRepo.delete(bannedUser);
    }

    private BannedUser getBannedUser(Long conversationId, Long userId) {
        return banRepo.findByConvIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not banned"));
    }

    private ConversationParticipant getParticipant(Long conversationId, Long userId) {
        return participantRepo.findByConversationAndUser(
                conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
    }

    @Override
    public boolean existsByConvIdAndUserId(Long conversationId, Long userId) {
        return banRepo.existsByConvIdAndUserId(conversationId, userId);
    }

}
