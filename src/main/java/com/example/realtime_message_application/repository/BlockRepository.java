package com.example.realtime_message_application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.realtime_message_application.model.Block;
import com.example.realtime_message_application.model.User;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    @Query("SELECT b FROM Block b WHERE b.blocker.userId = :blockerId")
    List<Block> findAllBlockedByBlocker(@Param("blockerId") Long blockerId);

    @Query("DELETE FROM Block b WHERE b.blocker.userId = :blockerId AND b.blocked.userId = :blockedId")
    void deleteByBlockerAndBlocked(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);
}