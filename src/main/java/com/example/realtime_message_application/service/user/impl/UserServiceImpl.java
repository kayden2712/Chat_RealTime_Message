package com.example.realtime_message_application.service.user.impl;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.realtime_message_application.dto.user.UserDTO;
import com.example.realtime_message_application.dto.user.UserResponse;
import com.example.realtime_message_application.dto.user.updateBio;
import com.example.realtime_message_application.dto.user.updateProfilePic;
import com.example.realtime_message_application.mapper.UserMapper;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.UserRepository;
import com.example.realtime_message_application.service.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::convertToUserResponse).toList();
    }

    @Override
    public UserResponse getUserById(Long userId) {
        return convertToUserResponse(findUserById(userId));
    }

    @Override
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return convertToUserResponse(user);
    }

    @Override
    public List<UserResponse> findByPhoneNo(String phoneNo) {
        return userRepository.findByPhoneNo(phoneNo).stream().map(this::convertToUserResponse).toList();
    }

    @Override
    public List<UserResponse> findByKeyword(String keyword) {
        return userRepository.findByKeyword(keyword).stream().map(this::convertToUserResponse).toList();
    }

    @Override
    public UserResponse addUser(UserDTO userDTO) {
        validateUser(userDTO);
        User user = convertToUser(userDTO, new User());
        userRepository.save(user);
        return convertToUserResponse(user);
    }

    @Override
    public UserResponse updateUser(Long userId, UserDTO userDTO) {
        User user = findUserById(userId);
        validateUser(userDTO);
        user = convertToUser(userDTO, user);
        return convertToUserResponse(user);
    }

    @Override
    public void deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public void disconnectUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        User user = findUserById(userId);
        user.setOnline(false);
        user.setLastSeen(Instant.now());
        userRepository.save(user);
    }

    @Override
    public void updateProfPic(updateProfilePic updatePic) {
        User user = findUserById(updatePic.getUserId());
        try {
            if (updatePic.getFile() != null && !updatePic.getFile().isEmpty()) {
                user.setProfilePicName(updatePic.getFile().getOriginalFilename());
                user.setImageType(updatePic.getFile().getContentType());
                user.setProfilePic(updatePic.getFile().getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update profile picture");
        }
    }

    @Override
    public void updateBio(updateBio userBio) {
        User user = findUserById(userBio.getUserId());
        user.setBio(userBio.getNewBio());
    }

    public User convertToUser(UserDTO userDTO, User user) {
        return userMapper.applyUser(user, userDTO);
    }

    public UserResponse convertToUserResponse(User user) {
        String lastSeen = user.isOnline() ? "Online" : lastSeenHelper(user);
        return userMapper.toResponse(user, lastSeen);
    }

    private String lastSeenHelper(User user) {
        Instant lastSeen = user.getLastSeen();
        if (lastSeen == null)
            return "Never seen!";

        long minutesAgo = Duration.between(lastSeen, Instant.now()).toMinutes();
        if (minutesAgo < 5) {
            return "last seen just now";
        } else if (minutesAgo < 60) {
            return "last seen " + minutesAgo + " minutes ago";
        } else if (minutesAgo < 720) {
            long hoursAgo = minutesAgo / 60;
            return "last seen " + hoursAgo + " hour ago";
        } else if (minutesAgo < 1440) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());
            return "last seen at today " + formatter.format(lastSeen);
        } else if (minutesAgo > 1440 && minutesAgo < 2880) {
            return "last seen yesterday";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d 'at' hh:mm a")
                    .withZone(ZoneId.systemDefault());
            return "last seen on " + formatter.format(lastSeen);
        }
    }

    private void validateUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByPhoneNo(userDTO.getPhoneNo())) {
            throw new RuntimeException("Phone number already exists");
        }

        if (userDTO.getEmail() != null && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
