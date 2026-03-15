package com.example.realtime_message_application.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.realtime_message_application.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

        @Query("SELECT m FROM Message m " +
                        "LEFT JOIN FETCH m.conversation c " +
                        "LEFT JOIN FETCH m.sender s " +
                        "LEFT JOIN FETCH m.replyTo r " +
                        "WHERE m.messageId = :messageId")
        Optional<Message> findByMessageIdWithDetails(@Param("messageId") int messageId);

        @Query("SELECT m FROM Message m WHERE m.conversation.conversationId = :conversationId")
        Page<Message> findByConversationId(Integer conversationId, Pageable pageable);

        @Query("SELECT m FROM Message m WHERE m.conversation.conversationId = :conversationId")
        List<Message> findAllMessagesByConversationId(@Param("conversationId") int conversationId);

        @Modifying
        @Query(value = "DELETE FROM Message m WHERE m.message_id = :messageId", nativeQuery = true)
        void permanentDeletionOfMessage(@Param("messageId") int messageId);

        @Query(value = "SELECT * FROM Message m " +
                        "WHERE m.message_id = :messageId " +
                        "AND m.is_deleted = true", nativeQuery = true)
        Optional<Message> findDeletedMessage(@Param("messageId") int messageId);

        @Query("SELECT m FROM Message m " +
                        "LEFT JOIN m.sender " +
                        "WHERE m.replyTo.messageId = :messageId ")
        Page<Message> findReplies(@Param("messageId") int messageId, Pageable pageable);

        @Query("SELECT m FROM Message m " +
                        "LEFT JOIN m.replies r " +
                        "WHERE m.replyTo.messageId = :messageId AND m.isDeleted = false")
        List<Message> findMessageReplies(@Param("messageId") int messageId);

        @Query("SELECT m FROM Message m LEFT JOIN FETCH m.replies WHERE m.messageId = :messageId")
        Message findMessageByIdWithReplies(@Param("messageId") int messageId);

        // Query for finding all message Ids that do have replies but not replying to
        // any message in a conversation
        // in other words these are independent messages, that are not replies to any
        // messages
        @Query("SELECT m.messageId FROM Message m LEFT JOIN m.replyTo " +
                        "WHERE m.conversation.conversationId = :conversationId " +
                        "AND m.replyTo IS NULL")
        List<Integer> findAllMessagesWithNoReplyInConv(@Param("conversationId") int conversationId);

        @Query("SELECT m FROM Message m " +
                        "LEFT JOIN m.conversation c " +
                        "WHERE m.conversation.conversationId = :conversationId " +
                        "AND m.pinned = true")
        List<Message> findPinnedMessagesInConv(@Param("conversationId") int conversationId);

        @Query("SELECT m FROM Message m WHERE m.expiresAt <= :now")
        List<Message> findExpiredMessages(@Param("now") LocalDateTime now);
}