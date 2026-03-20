package com.example.realtime_message_application.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.realtime_message_application.dto.conversation.ParticipantResponse;
import com.example.realtime_message_application.service.ParticipantService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    @GetMapping("/{conversationId}/participants")
    public ResponseEntity<List<ParticipantResponse>> getAllParticipantsInConv(@PathVariable Long conversationId) {
        return ResponseEntity.ok(participantService.getAllParticipantsInConv(conversationId));
    }

    @GetMapping("/{conversationId}/admins")
    public ResponseEntity<Long> getAdminsCountInConv(@PathVariable Long conversationId) {
        return ResponseEntity.ok(participantService.getAdminsCountInConv(conversationId));
    }

    @GetMapping("/{conversationId}/members")
    public ResponseEntity<Long> getMembersCountInConv(@PathVariable Long conversationId) {
        return ResponseEntity.ok(participantService.getMembersCountInConv(conversationId));
    }

}
