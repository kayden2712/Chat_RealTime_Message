package com.example.realtime_message_application.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.realtime_message_application.enums.ParticipantRole;
import com.example.realtime_message_application.model.ConversationParticipant;

@Repository
public interface ParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

        @Query("SELECT p FROM ConversationParticipant p WHERE p.conversation.conversationId = :conversationId")
        List<ConversationParticipant> findByConversationId(@Param("conversationId") Long conversationId);

        @Query("SELECT p FROM ConversationParticipant p WHERE p.user.userId = :userId")
        Optional<ConversationParticipant> findByUserId(@Param("userId") Long userId);

        @Query("SELECT p FROM ConversationParticipant p WHERE p.conversation.conversationId = :conversationId AND p.user.userId = :userId")
        Optional<ConversationParticipant> findByConversationAndUser(@Param("conversationId") Long conversationId,
                        @Param("userId") Long userId);

        @Query("SELECT p FROM ConversationParticipant p WHERE p.conversation.conversationId = :conversationId")
        Set<ConversationParticipant> findAllParticipantsByConversationId(@Param("conversationId") Long conversationId);

        @Query("SELECT COUNT(p) FROM ConversationParticipant p WHERE p.conversation.conversationId = :conversationId AND p.participantRole = :role")
        Long countNoOfAdminsInConversation(
                        @Param("conversationId") Long conversationId,
                        @Param("role") ParticipantRole role);

        @Query("SELECT COUNT(p) FROM ConversationParticipant p WHERE p.conversation.conversationId = :conversationId AND p.participantRole = 'ADMIN'")
        Long countNoOfAdminsInConv(@Param("conversationId") Long conversationId);

        @Query("SELECT COUNT(p) FROM ConversationParticipant p WHERE p.conversation.conversationId = :conversationId")
        Long countNoOfParticipantsInConv(@Param("conversationId") Long conversationId);

        @Query("SELECT COUNT(p) FROM ConversationParticipant p WHERE p.conversation.conversationId = :conversationId AND p.participantRole = 'MEMBER'")
        Long countNoOfMembersInConv(@Param("conversationId") Long conversationId);

        @Query("SELECT p FROM ConversationParticipant p WHERE p.user.userId = :userId AND p.isFavorite = true")
        List<ConversationParticipant> findAllFavoriteByUserId(@Param("userId") Long userId);

        @Query("SELECT p FROM ConversationParticipant p WHERE p.user.userId = :userId AND p.isArchived = true")
        List<ConversationParticipant> findAllArchivedByUserId(@Param("userId") Long userId);

        @Query("DELETE FROM ConversationParticipant p WHERE p.conversation.conversationId = :conversationId AND p.user.userId = :userId")
        void deleteByConversation_ConversationIdAndUser_UserId(
                        @Param("conversationId") Long conversationId,
                        @Param("userId") Long userId);

        @Query("SELECT COUNT(p) FROM ConversationParticipant p WHERE p.conversation.conversationId = :conversationId AND p.user.userId = :userId")
        boolean existsByConversationIdAndUserId(
                        @Param("conversationId") Long conversationId,
                        @Param("userId") Long userId);

}
