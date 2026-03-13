package com.example.realtime_message_application.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.realtime_message_application.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"conversation","sender","replyTo","replies"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@FilterDef(name = "deletedMessageFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "deletedMessageFilter", condition = "isDeleted = false")
@EntityListeners(AuditingEntityListener.class)
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long messageId;

    @Column(columnDefinition = "text")
    private String content;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Conversation conversation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User sender;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant editedAt;

    @ManyToOne
    @JoinColumn(name = "reply_to_id")
    @JsonBackReference("message-replies")
    private Message replyTo;

    @OneToMany(mappedBy = "replyTo",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    @JsonManagedReference("message-replies")   // marks this as the forward reference
    private Set<Message> replies = new HashSet<>();

    //soft delete
    @SQLDelete(sql = "UPDATE Message SET is_deleted = true WHERE message_id = ?")
    private boolean isDeleted = false;
    private String deletedBy;

    private LocalDateTime expiresAt;

    private boolean pinned = false; //ghim

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    public void addReply(Message reply){
        replies.add(reply);
        reply.setReplyTo(this);
    }

    public void removeReply(Message reply){
        replies.remove(reply);
        reply.setReplyTo(null);
    }

}
