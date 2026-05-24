package com.example.realtime_message_application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.realtime_message_application.service.TestDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestDataController {

    private final TestDataService testDataService;

    @PostMapping("/seed")
    public ResponseEntity<?> seedRandomData() {
        TestDataService.SeedResult result = testDataService.seedRandomData(10, 10, 100);
        return ResponseEntity.ok(result);
    }
}
