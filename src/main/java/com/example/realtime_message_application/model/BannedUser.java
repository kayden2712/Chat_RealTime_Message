package com.example.realtime_message_application.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "banned_users")
public class BannedUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bannedUserId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;
    private String bannedBy;
    private String reason;

    @Builder.Default
    private Instant bannedAt = Instant.now();

    @Builder.Default
    private boolean permanently = true;
}
