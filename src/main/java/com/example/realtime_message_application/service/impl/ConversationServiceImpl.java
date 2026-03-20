package com.example.realtime_message_application.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.realtime_message_application.dto.conversation.AddParticipant;
import com.example.realtime_message_application.dto.conversation.ArchiveConv;
import com.example.realtime_message_application.dto.conversation.ConversationDTO;
import com.example.realtime_message_application.dto.conversation.ConversationResponse;
import com.example.realtime_message_application.dto.conversation.FavoriteConv;
import com.example.realtime_message_application.dto.conversation.LeaveConversation;
import com.example.realtime_message_application.dto.conversation.MuteConv;
import com.example.realtime_message_application.dto.conversation.RemoveParticipant;
import com.example.realtime_message_application.dto.conversation.UpdateConvDescription;
import com.example.realtime_message_application.dto.conversation.UpdateConvImage;
import com.example.realtime_message_application.dto.conversation.UpdateConvRole;
import com.example.realtime_message_application.dto.conversation.UpdateConvTitle;
import com.example.realtime_message_application.enums.ConversationType;
import com.example.realtime_message_application.enums.ParticipantRole;
import com.example.realtime_message_application.mapper.ConversationMapper;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.model.ConversationParticipant;
import com.example.realtime_message_application.model.Message;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.BanRepository;
import com.example.realtime_message_application.repository.BlockRepository;
import com.example.realtime_message_application.repository.ConversationRepository;
import com.example.realtime_message_application.repository.MessageRepository;
import com.example.realtime_message_application.repository.ParticipantRepository;
import com.example.realtime_message_application.service.ConversationService;
import com.example.realtime_message_application.service.UserService;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ParticipantRepository participantRepository;
    private final BanRepository banRepository;
    private final BlockRepository blockRepository;
    private final UserService userService;
    private final ConversationMapper conversationMapper;
    private final TaskScheduler taskScheduler;

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getAllConversations() {
        return conversationRepository.findAll().stream().map(this::toConversationResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getAllConversationByUserId(Long userId) {
        return conversationRepository.findAllConversationsByUserId(userId).stream().map(this::toConversationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationByTitle(String title) {
        return conversationRepository.findByConversationName(title).stream().map(this::toConversationResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(Long conversationId) {
        return toConversationResponse(getEntityByConvId(conversationId));
    }

    @Override
    public ConversationResponse createConversation(ConversationDTO conversation, MultipartFile image) {
        Conversation conv = toConversation(conversation, image);
        return convertToConversationResponse(conv);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getAllMessagesByConversationId(Long conversationId) {
        List<Message> list = messageRepository.findAllMessagesByConversationId(conversationId);
        return list.stream().map(Message::getMessageId).toList();
    }

    @Override
    public void removeAllConversationByUserId(Long userId) {
        List<Conversation> list = conversationRepository.findAllConversationsByUserId(userId);
        conversationRepository.deleteAll(list);
    }

    @Override
    public void removeConversationById(Long conversationId) {
        conversationRepository.deleteById(conversationId);
    }

    @Override
    public void removeAllMessageInConversation(Long conversationId) {
        List<Message> list = messageRepository.findAllMessagesByConversationId(conversationId);
        messageRepository.deleteAll(list);
    }

    @Override
    public void removedConversationByTitle(String title) {
        List<Conversation> list = conversationRepository.findByConversationName(title);
        conversationRepository.deleteAll(list);
    }

    @Override
    public ConversationResponse AddParticipantInConversation(AddParticipant addParticipant) {

        Conversation conv = getEntityByConvId(addParticipant.conversationId());
        User member = userService.getEntityByUserId(addParticipant.userId());
        User admin = userService.getEntityByUserId(addParticipant.adminId());

        if (conv.getType() == ConversationType.PRIVATE) {
            throw new RuntimeException("Can't add people to an ONE to ONE chat.");
        }

        if (participantRepository.findByConversationAndUser(conv.getConversationId(), member.getUserId()).isPresent()) {
            throw new RuntimeException("User is already a participant in the conversation.");
        }

        ConversationParticipant participant = ConversationParticipant.builder()
                .conversation(conv)
                .user(member)
                .participantRole(
                        addParticipant.role() == ParticipantRole.ADMIN ? ParticipantRole.ADMIN : ParticipantRole.MEMBER)
                .addedBy(admin)
                .build();
        participantRepository.save(participant);

        return convertToConversationResponse(participant.getConversation());
    }

    @Override
    public ConversationResponse removeParticipantInConversation(RemoveParticipant removeParticipant) {
        Conversation conv = getEntityByConvId(removeParticipant.conversationId());
        ConversationParticipant member = getEntityByConvIdAndUserId(removeParticipant.conversationId(),
                removeParticipant.userId());
        ConversationParticipant admin = getEntityByConvIdAndUserId(removeParticipant.conversationId(),
                removeParticipant.adminId());

        if (conv.getType() == ConversationType.PRIVATE) {
            throw new RuntimeException("Can't remove people from an ONE to ONE chat.");
        }

        if (admin.getParticipantRole() != ParticipantRole.ADMIN) {
            throw new RuntimeException("You are not authorized to remove people from this conversation.");
        }

        if (member == null) {
            throw new RuntimeException("User is not a participant in the conversation.");
        }
        participantRepository.delete(member);

        return convertToConversationResponse(conv);
    }

    @Override
    public String leaveConversation(LeaveConversation leaveConversation) {
        ConversationParticipant participant = getEntityByConvIdAndUserId(leaveConversation.conversationId(),
                leaveConversation.userId());

        if (participant == null) {
            throw new RuntimeException("You don't in here.");
        }

        if (participant.getConversation().getType() == ConversationType.PRIVATE) {
            throw new RuntimeException("You can't leave a private conversation.");
        }

        participantRepository.delete(participant);
        return "User left the conversation";
    }

    @Override
    public ConversationResponse updateConversationTitle(UpdateConvTitle updateConvTitle) {
        Conversation conv = getEntityByConvId(updateConvTitle.conversationId());

        if (conv.getType() == ConversationType.PRIVATE) {
            throw new RuntimeException("You can't update title of a private conversation.");
        }

        conv.setTitle(updateConvTitle.title());

        return convertToConversationResponse(conv);
    }

    @Override
    public ConversationResponse updateConversationImage(UpdateConvImage updateConvImage) {
        Conversation conv = getEntityByConvId(updateConvImage.getConversationId());

        if (conv.getType() == ConversationType.PRIVATE) {
            throw new RuntimeException("You can't update image of a private conversation.");
        }

        if (updateConvImage.getImage() != null || !updateConvImage.getImage().isEmpty()) {
            try {
                String fileName = updateConvImage.getImage().getOriginalFilename();
                Path path = Paths.get(System.getProperty("user.dir") + "\\uploads\\" + fileName);
                Files.write(path, updateConvImage.getImage().getBytes());
                conv.setAvatarUrl(path.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return convertToConversationResponse(conv);
    }

    @Override
    public ConversationResponse updateConversationRole(UpdateConvRole updateConvRole) {
        ConversationParticipant participant = getEntityByConvIdAndUserId(updateConvRole.conversationId(),
                updateConvRole.userId());
        ConversationParticipant admin = getEntityByConvIdAndUserId(updateConvRole.conversationId(),
                updateConvRole.adminId());

        if (participant == null) {
            throw new RuntimeException("Member don't in here.");
        }

        if (admin.getParticipantRole() != ParticipantRole.ADMIN) {
            throw new RuntimeException("You are not authorized to update role of people in this conversation.");
        }

        participant.setParticipantRole(updateConvRole.role());
        return convertToConversationResponse(participant.getConversation());
    }

    @Override
    public ConversationResponse updateConversationDescription(UpdateConvDescription updateConvDescription) {
        Conversation conv = getEntityByConvId(updateConvDescription.conversationId());

        if (conv.getType() == ConversationType.PRIVATE) {
            throw new RuntimeException("You can't update description of a private conversation.");
        }

        conv.setDescription(updateConvDescription.description());
        return convertToConversationResponse(conv);
    }

    @Override
    public void muteConversation(MuteConv muteConv) {
        ConversationParticipant participant = getEntityByConvIdAndUserId(muteConv.conversationId(),
                muteConv.userId());

        if (participant == null) {
            throw new RuntimeException("You don't in here.");
        }

        if (participant.isMuted()) {
            throw new RuntimeException("You are already muted.");
        }

        participant.setMuted(true);

        Integer minutes = muteConv.durationInMinutes();

        // Nếu có thời hạn, lập lịch để Unmute
        if (minutes != null && minutes > 0) {
            long delayInMillis = (long) minutes * 60 * 1000;

            // Lưu ID lại để dùng cho luồng sau
            Long participantId = participant.getCpId();

            taskScheduler.schedule(() -> {
                // Quan trọng: Phải tìm lại Participant từ Database bằng ID
                // để đảm bảo nó là một Entity "Fresh" trong luồng mới.
                participantRepository.findById(participantId).ifPresent(p -> {
                    // Kiểm tra nếu người dùng vẫn đang bị Mute thì mới mở (tránh ghi đè nếu họ đã
                    // chủ động mở trước đó)
                    if (p.isMuted()) {
                        p.setMuted(false);
                        participantRepository.save(p);
                        System.out.println("Hệ thống: Tự động bật lại thông báo cho Participant ID: " + participantId);
                    }
                });

            }, Instant.now().plusMillis(delayInMillis));
        }
    }

    @Override
    public void unMuteConversation(MuteConv unMuteConv) {
        ConversationParticipant participant = getEntityByConvIdAndUserId(unMuteConv.conversationId(),
                unMuteConv.userId());

        if (participant == null) {
            throw new RuntimeException("You don't in here.");
        }

        if (!participant.isMuted()) {
            throw new RuntimeException("You are not muted.");
        }

        participant.setMuted(false);
        participantRepository.save(participant);
    }

    @Override
    public void addArchiveConversation(ArchiveConv archiveConv) {
        ConversationParticipant participant = getEntityByConvIdAndUserId(archiveConv.conversationId(),
                archiveConv.userId());

        if (participant == null) {
            throw new RuntimeException("You don't in here.");
        }

        if (participant.isArchived()) {
            throw new RuntimeException("You are already archived.");
        }

        participant.setArchived(true);
        participantRepository.save(participant);
    }

    @Override
    public void removeArchiveConversation(ArchiveConv archiveConv) {
        ConversationParticipant participant = getEntityByConvIdAndUserId(archiveConv.conversationId(),
                archiveConv.userId());

        if (participant == null) {
            throw new RuntimeException("You don't in here.");
        }

        if (!participant.isArchived()) {
            throw new RuntimeException("You are not archived.");
        }

        participant.setArchived(false);
    }

    @Override
    public void addFavoriteConversation(FavoriteConv favoriteConv) {
        ConversationParticipant participant = getEntityByConvIdAndUserId(favoriteConv.conversationId(),
                favoriteConv.userId());

        if (participant == null) {
            throw new RuntimeException("You don't in here.");
        }

        if (participant.isFavorite()) {
            throw new RuntimeException("You are already favorited.");
        }

        participant.setFavorite(true);
    }

    @Override
    public void removeFavoriteConversation(FavoriteConv favoriteConv) {
        ConversationParticipant participant = getEntityByConvIdAndUserId(favoriteConv.conversationId(),
                favoriteConv.userId());

        if (participant == null) {
            throw new RuntimeException("You don't in here.");
        }

        if (!participant.isFavorite()) {
            throw new RuntimeException("You are not favorited.");
        }

        participant.setFavorite(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getAllFavoriteConversation(Long userId) {
        List<ConversationParticipant> list = participantRepository.findAllFavoriteByUserId(userId);
        return list.stream().map(participant -> toConversationResponse(participant.getConversation())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getAllArchivedConversation(Long userId) {
        List<ConversationParticipant> list = participantRepository.findAllArchivedByUserId(userId);
        return list.stream().map(participant -> toConversationResponse(participant.getConversation())).toList();
    }

    @Override
    public void enableDisappearing(Long convId, int days) {
        Conversation conversation = conversationRepository.findById(convId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.updateDisappearingSettings(true, days);
        conversationRepository.save(conversation);
    }

    @Override
    public void disableDisappearing(Long convId) {
        Conversation conversation = conversationRepository.findById(convId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.updateDisappearingSettings(false, 0);
        conversationRepository.save(conversation);
    }

    @Override
    public ConversationResponse createPrivateGroup(Long userId1, Long userId2) {

        // SELF CONVERSATION
        if (userId1 == userId2) {
            Optional<Conversation> convSelf = conversationRepository.findSelfConversation(userId1);
            if (convSelf.isPresent()) {
                return toConversationResponse(convSelf.get());
            }

            User user = userService.getEntityByUserId(userId1);

            Conversation conversation = Conversation.builder()
                    .title(user.getUsername())
                    .description("My cloud")
                    .type(ConversationType.PRIVATE)
                    .creator(user)
                    .build();
            Conversation savedConv = conversationRepository.save(conversation);

            ConversationParticipant self = ConversationParticipant.builder()
                    .conversation(savedConv)
                    .user(user)
                    .participantRole(ParticipantRole.ADMIN)
                    .build();
            participantRepository.save(self);
            return toConversationResponse(savedConv);
        }

        // 1 to 1 conversation
        Optional<Conversation> convPrivate = conversationRepository.findPrivateConvBetweenTwoUsers(userId1, userId2);
        if (convPrivate.isPresent()) {
            return toConversationResponse(convPrivate.get());
        }

        User user1 = userService.getEntityByUserId(userId1);
        User user2 = userService.getEntityByUserId(userId2);

        Conversation conversation = Conversation.builder()
                .title(user1.getUsername() + " and " + user2.getUsername())
                .description("Private conversation between " + user1.getUsername() + " and " + user2.getUsername())
                .type(ConversationType.PRIVATE)
                .creator(user1)
                .build();
        Conversation savedConv = conversationRepository.save(conversation);

        ConversationParticipant participant1 = ConversationParticipant.builder()
                .conversation(savedConv)
                .user(user1)
                .participantRole(ParticipantRole.ADMIN)
                .build();
        participantRepository.save(participant1);

        ConversationParticipant participant2 = ConversationParticipant.builder()
                .conversation(savedConv)
                .user(user2)
                .participantRole(ParticipantRole.ADMIN)
                .build();
        participantRepository.save(participant2);

        return toConversationResponse(savedConv);
    }

    private ConversationResponse toConversationResponse(Conversation conversation) {
        List<ConversationParticipant> list = participantRepository
                .findByConversationId(conversation.getConversationId());
        return conversationMapper.toConversationResponse(conversation, list);
    }

    private Conversation toConversation(ConversationDTO conversation, MultipartFile image) {
        Conversation conv = new Conversation();
        conv.setTitle(conversation.getTitle());
        conv.setDescription(conversation.getDescription());
        conv.setType(conversation.getType());

        User creator = userService.getEntityByUserId(conversation.getCreatorId());
        conv.setCreator(creator);

        if (image != null || !image.isEmpty()) {
            try {
                String fileName = image.getOriginalFilename();
                Path path = Paths.get(System.getProperty("user.dir") + "\\uploads\\" + fileName);
                Files.write(path, image.getBytes());
                System.out.println("\nImage uploaded successfully\n");
                String avaterUrl = path.toString();
                conv.setAvatarUrl(avaterUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image");
            }
        }

        Conversation savedConv = conversationRepository.save(conv);

        // creator as first participant
        ConversationParticipant participant = ConversationParticipant.builder()
                .conversation(savedConv)
                .user(conv.getCreator())
                .participantRole(ParticipantRole.ADMIN) // creator is always admin
                .joinedOn(Instant.now())
                .build();
        participantRepository.save(participant);

        // add participants next if exists
        if (conversation.getParticipantsId() != null && !conversation.getParticipantsId().isEmpty()) {
            for (Long participantId : conversation.getParticipantsId()) {
                User user = userService.getEntityByUserId(participantId);
                ConversationParticipant member = ConversationParticipant.builder()
                        .conversation(savedConv)
                        .user(user)
                        .participantRole(ParticipantRole.MEMBER)
                        .addedBy(conv.getCreator())
                        .build();
                participantRepository.save(member);
            }
        }
        return conv;
    }

    public ConversationResponse convertToConversationResponse(Conversation conversation) {
        List<ConversationParticipant> list = participantRepository
                .findByConversationId(conversation.getConversationId());
        return conversationMapper.toConversationResponse(conversation, list);
    }

    public Conversation getEntityByConvId(Long convId) {
        return conversationRepository.findById(convId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    public ConversationParticipant getEntityByConvIdAndUserId(Long convId, Long userId) {
        return participantRepository.findByConversationAndUser(convId, userId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));
    }
}
