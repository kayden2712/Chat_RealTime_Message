package com.example.realtime_message_application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.realtime_message_application.dto.notification.FCMTokenRequest;
import com.example.realtime_message_application.service.FCMService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
public class FCMController {

    private final FCMService fcmService;

    @PostMapping("/register")
    public ResponseEntity<String> registerToken(@RequestBody FCMTokenRequest request) {
        fcmService.registerToken(request.userId(), request.token());
        return ResponseEntity.ok("Token registered successfully");
    }

    @DeleteMapping("/unregister")
    public ResponseEntity<String> unregisterToken(@RequestParam String token) {
        fcmService.deleteToken(token);
        return ResponseEntity.ok("Token unregistered successfully");
    }
}
