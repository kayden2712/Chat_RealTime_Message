package com.example.realtime_message_application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.realtime_message_application.model.FCMToken;
import com.example.realtime_message_application.model.User;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {
    List<FCMToken> findByUser_UserId(Long userId);

    Optional<FCMToken> findByUser(User user);

    Optional<FCMToken> findByToken(String token);

    void deleteByToken(String token);

    @Query("SELECT ft FROM FCMToken ft WHERE ft.user IN :participants")
    List<FCMToken> findAllByUserIn(@Param("participants") List<User> participants);
}
