package com.example.realtime_message_application.model;

import java.time.Instant;

import com.example.realtime_message_application.enums.ParticipantRole;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cpId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    @JsonBackReference
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User user;

    @Enumerated(EnumType.STRING)
    private ParticipantRole participantRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_id")
    @JsonIgnore
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User addedBy;

    @Builder.Default
    private boolean isMuted = false;
    @Builder.Default
    private boolean isFavorite = false;
    @Builder.Default
    private boolean isArchived = false;
    @Builder.Default
    private Instant joinedOn = Instant.now();
}
