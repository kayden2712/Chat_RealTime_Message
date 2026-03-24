package com.example.realtime_message_application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.realtime_message_application.model.ReadReceipt;

public interface ReadReceiptRepository extends JpaRepository<ReadReceipt, Long> {

    @Query("SELECT COUNT(r) > 0 FROM ReadReceipt r " +
            "WHERE r.message.messageId = :messageId " +
            "AND r.user.userId = :readerId")
    boolean existsByMessageIdAndReaderId(@Param("messageId") Long messageId, @Param("readerId") Long readerId);

    @Query("SELECT r FROM ReadReceipt r WHERE r.message.messageId = :messageId")
    List<ReadReceipt> findAllReadersForMessage(@Param("messageId") Long messageId);
}
