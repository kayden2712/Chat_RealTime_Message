package com.example.realtime_message_application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.realtime_message_application.dto.conversation.BanUserDTO;
import com.example.realtime_message_application.service.BanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ban")
@RequiredArgsConstructor
public class BanController {
    private final BanService banService;

    @PostMapping("/ban")
    public ResponseEntity<?> banUser(@RequestBody BanUserDTO banUserDTO) {
        banService.banUser(banUserDTO);
        return ResponseEntity.ok("User banned successfully.");
    }

    @PostMapping("/unban")
    public ResponseEntity<?> unbanUser(@RequestBody BanUserDTO banUserDTO) {
        banService.unbanUser(banUserDTO.conversationId(), banUserDTO.targetUserId(), banUserDTO.adminId());
        return ResponseEntity.ok("User unbanned successfully.");
    }
}
