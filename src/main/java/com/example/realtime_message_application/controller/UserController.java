package com.example.realtime_message_application.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.realtime_message_application.dto.user.UserDTO;
import com.example.realtime_message_application.dto.user.UserResponse;
import com.example.realtime_message_application.dto.user.updateBio;
import com.example.realtime_message_application.dto.user.updateProfilePic;
import com.example.realtime_message_application.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> findByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }

    @GetMapping("/phone/{phoneNo}")
    public ResponseEntity<List<UserResponse>> findByPhoneNo(@PathVariable String phoneNo) {
        return ResponseEntity.ok(userService.findByPhoneNo(phoneNo));
    }

    @GetMapping("/keyword/{keyword}")
    public ResponseEntity<List<UserResponse>> findByKeyword(@PathVariable String keyword) {
        return ResponseEntity.ok(userService.findByKeyword(keyword));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.addUser(userDTO));
    }

    @PutMapping("update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(userId, userDTO));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUserById(@PathVariable Long userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok().body("User deleted successfully.");
    }

    @PutMapping("/disconnect/{userId}")
    public ResponseEntity<?> disconnectUser(@PathVariable Long userId) {
        userService.disconnectUser(userId);
        return ResponseEntity.ok().body("User disconnected successfully.");
    }

    @PostMapping("update/profile-pic")
    public ResponseEntity<?> updateProfPic(@RequestBody updateProfilePic updatePic) {
        userService.updateProfPic(updatePic);
        return ResponseEntity.ok().body("Profile pic updated successfully.");
    }

    @PutMapping("update/bio")
    public ResponseEntity<?> updateBio(@RequestBody updateBio userBio) {
        userService.updateBio(userBio);
        return ResponseEntity.ok().body("Bio updated successfully.");
    }

}
