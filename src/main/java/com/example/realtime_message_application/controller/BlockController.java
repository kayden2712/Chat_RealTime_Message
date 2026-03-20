package com.example.realtime_message_application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.realtime_message_application.dto.conversation.BlockingDTO;
import com.example.realtime_message_application.service.BlockedService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/block")
@RequiredArgsConstructor
public class BlockController {
    private final BlockedService blockedService;

    @PostMapping("/blocking")
    public ResponseEntity<?> blockingUser(@RequestBody BlockingDTO blockingDTO) {
        blockedService.blockUser(blockingDTO);
        return ResponseEntity.ok().body("User blocked successfully.");
    }

    @PostMapping("/unblocking")
    public ResponseEntity<?> unblockingUser(@RequestBody BlockingDTO blockUserDTO) {
        blockedService.unblockUser(blockUserDTO);
        return ResponseEntity.ok().body("User unblocked successfully.");
    }

    @GetMapping("/blocked-list/{blockerId}")
    public ResponseEntity<?> getBlockedList(@PathVariable Long blockerId) {
        return ResponseEntity.ok(blockedService.getBlockedList(blockerId));
    }
}
