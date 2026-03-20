package com.example.realtime_message_application.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
import com.example.realtime_message_application.service.ConversationService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConversationResponse>> getAllConversation() {
        return ResponseEntity.ok(conversationService.getAllConversations());
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<List<ConversationResponse>> getAllConversationByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(conversationService.getAllConversationByUserId(userId));
    }

    @GetMapping("/all/{title}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConversationResponse>> getAllConversationByTitle(@PathVariable String title) {
        return ResponseEntity.ok(conversationService.getConversationByTitle(title));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> getAllConversationById(@PathVariable Long conversationId) {
        return ResponseEntity.ok(conversationService.getConversationById(conversationId));
    }

    @PostMapping("/create")
    public ResponseEntity<ConversationResponse> createConversation(@RequestBody ConversationDTO conversation,
            MultipartFile image) {
        return ResponseEntity.ok(conversationService.createConversation(conversation, image));
    }

    @GetMapping("/all/messages/{conversationId}")
    public ResponseEntity<List<Long>> getAllMessagesByConversationId(@PathVariable Long conversationId) {
        return ResponseEntity.ok(conversationService.getAllMessagesByConversationId(conversationId));
    }

    @DeleteMapping("/all/{userId}")
    public ResponseEntity<?> removeAllConversationByUserId(@PathVariable Long userId) {
        conversationService.removeAllConversationByUserId(userId);
        return ResponseEntity.ok().body("All conversations removed successfully.");
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<?> removeConversationById(@PathVariable Long conversationId) {
        conversationService.removeConversationById(conversationId);
        return ResponseEntity.ok().body("Conversation removed successfully.");
    }

    @DeleteMapping("/all/messages/{conversationId}")
    public ResponseEntity<?> removeAllMessageInConversation(@PathVariable Long conversationId) {
        conversationService.removeAllMessageInConversation(conversationId);
        return ResponseEntity.ok().body("All messages in conversation removed successfully.");
    }

    @DeleteMapping("/all/title/{title}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removedConversationByTitle(@PathVariable String title) {
        conversationService.removedConversationByTitle(title);
        return ResponseEntity.ok().body("Conversation removed successfully.");
    }

    @PostMapping("/add/participant")
    public ResponseEntity<?> addParticipantInConversation(@RequestBody AddParticipant addParticipant) {
        return ResponseEntity.ok(conversationService.AddParticipantInConversation(addParticipant));
    }

    @PostMapping("/remove/participant")
    public ResponseEntity<?> removeParticipantInConversation(@RequestBody RemoveParticipant removeParticipant) {
        return ResponseEntity.ok(conversationService.removeParticipantInConversation(removeParticipant));
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveConversation(@RequestBody LeaveConversation leaveConversation) {
        return ResponseEntity.ok(conversationService.leaveConversation(leaveConversation));
    }

    @PostMapping("/update/title")
    public ResponseEntity<?> updateConversationTitle(@RequestBody UpdateConvTitle updateConvTitle) {
        return ResponseEntity.ok(conversationService.updateConversationTitle(updateConvTitle));
    }

    @PostMapping("/update/image")
    public ResponseEntity<?> updateConversationImage(@RequestBody UpdateConvImage updateConvImage) {
        return ResponseEntity.ok(conversationService.updateConversationImage(updateConvImage));
    }

    @PostMapping("/update/role")
    public ResponseEntity<?> updateConversationRole(@RequestBody UpdateConvRole updateConvRole) {
        return ResponseEntity.ok(conversationService.updateConversationRole(updateConvRole));
    }

    @PostMapping("/update/description")
    public ResponseEntity<?> updateConversationDescription(@RequestBody UpdateConvDescription updateConvDescription) {
        return ResponseEntity.ok(conversationService.updateConversationDescription(updateConvDescription));
    }

    @PostMapping("/mute")
    public ResponseEntity<?> muteConversation(@RequestBody MuteConv muteConv) {
        conversationService.muteConversation(muteConv);
        return ResponseEntity.ok().body("Conversation muted successfully.");
    }

    @PostMapping("/unmute")
    public ResponseEntity<?> unMuteConversation(@RequestBody MuteConv unMuteConv) {
        conversationService.unMuteConversation(unMuteConv);
        return ResponseEntity.ok().body("Conversation unmuted successfully.");
    }

    @PostMapping("/add/archive")
    public ResponseEntity<?> addArchiveConversation(@RequestBody ArchiveConv archiveConv) {
        conversationService.addArchiveConversation(archiveConv);
        return ResponseEntity.ok().body("Conversation archived successfully.");
    }

    @PostMapping("/remove/archive")
    public ResponseEntity<?> removeArchiveConversation(@RequestBody ArchiveConv archiveConv) {
        conversationService.removeArchiveConversation(archiveConv);
        return ResponseEntity.ok().body("Conversation unarchived successfully.");
    }

    @PostMapping("/add/favorite")
    public ResponseEntity<?> addFavoriteConversation(@RequestBody FavoriteConv favoriteConv) {
        conversationService.addFavoriteConversation(favoriteConv);
        return ResponseEntity.ok().body("Conversation favorited successfully.");
    }

    @PostMapping("/remove/favorite")
    public ResponseEntity<?> removeFavoriteConversation(@RequestBody FavoriteConv favoriteConv) {
        conversationService.removeFavoriteConversation(favoriteConv);
        return ResponseEntity.ok().body("Conversation unfavorited successfully.");
    }

    @GetMapping("/all/favorite/{userId}")
    public ResponseEntity<List<ConversationResponse>> getAllFavoriteConversation(@PathVariable Long userId) {
        return ResponseEntity.ok(conversationService.getAllFavoriteConversation(userId));
    }

    @GetMapping("/all/archive/{userId}")
    public ResponseEntity<List<ConversationResponse>> getAllArchivedConversation(@PathVariable Long userId) {
        return ResponseEntity.ok(conversationService.getAllArchivedConversation(userId));
    }

    @PostMapping("/enable/disappearing")
    public ResponseEntity<?> enableDisappearing(@PathVariable Long convId, @PathVariable int days) {
        conversationService.enableDisappearing(convId, days);
        return ResponseEntity.ok().body("Disappearing enabled successfully.");
    }

    @PostMapping("/disable/disappearing")
    public ResponseEntity<?> disableDisappearing(@PathVariable Long convId) {
        conversationService.disableDisappearing(convId);
        return ResponseEntity.ok().body("Disappearing disabled successfully.");
    }

    @PostMapping("/create/private/group")
    public ResponseEntity<ConversationResponse> createPrivateGroup(@PathVariable Long userId1, @PathVariable Long userId2) {
        return ResponseEntity.ok(conversationService.createPrivateGroup(userId1, userId2));
    }

}
