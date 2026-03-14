package com.example.realtime_message_application.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.realtime_message_application.model.BannedUser;

@Repository
public interface BanRepository extends JpaRepository<BannedUser, Long> {

    @Query("SELECT COUNT(b) > 0 FROM BannedUser b WHERE b.conversation.conversationId = :conversationId AND b.user.userId = :userId")
    boolean existsByConvIdAndUserId(@Param("conversationId") Long convId, @Param("userId") Long userId);

    @Query("SELECT b FROM BannedUser b WHERE b.conversation.conversationId = :conversationId AND b.user.userId = :userId")
    Optional<BannedUser> findByConvIdAndUserId(@Param("conversationId") Long convId, @Param("userId") Long userId);
}
