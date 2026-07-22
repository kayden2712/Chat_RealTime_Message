package com.example.realtime_message_application.service.impl;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.realtime_message_application.config.RedisConfig;
import com.example.realtime_message_application.dto.user.UserDTO;
import com.example.realtime_message_application.dto.user.UserResponse;
import com.example.realtime_message_application.dto.user.updateBio;
import com.example.realtime_message_application.dto.user.updateProfilePic;
import com.example.realtime_message_application.exception.ConflictException;
import com.example.realtime_message_application.exception.ResourceNotFoundException;
import com.example.realtime_message_application.mapper.UserMapper;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.UserRepository;
import com.example.realtime_message_application.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::convertToUserResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_USERS, key = "#userId")
    public UserResponse getUserById(Long userId) {
        return convertToUserResponse(getEntityByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_USER_BY_NAME, key = "#username")
    public UserResponse findByUsername(String username) {
        User user = userRepository.loadByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findByPhoneNo(String phoneNo) {
        return userRepository.findByPhoneNo(phoneNo).stream().map(this::convertToUserResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findByKeyword(String keyword) {
        return userRepository.findByKeyword(keyword).stream().map(this::convertToUserResponse).toList();
    }

    @Override
    public UserResponse addUser(UserDTO userDTO) {
        validateUser(userDTO);
        User user = new User();
        userMapper.applyUser(user, userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = RedisConfig.CACHE_USERS, key = "#userId"),
        @CacheEvict(value = RedisConfig.CACHE_USER_BY_NAME, allEntries = true)
    })
    public UserResponse updateUser(Long userId, UserDTO userDTO) {
        User user = getEntityByUserId(userId);
        validateUserForUpdate(userId, userDTO);

        String oldPassword = user.getPassword();
        userMapper.applyUser(user, userDTO);

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()
                && !passwordEncoder.matches(userDTO.getPassword(), oldPassword)) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        } else {
            user.setPassword(oldPassword);
        }

        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    private void validateUserForUpdate(Long userId, UserDTO userDTO) {
        if (!userRepository.existsByUsername(userDTO.getUsername())) {
            throw new ConflictException("Username already exists");
        }

        userRepository.findByPhoneNo(userDTO.getPhoneNo()).stream()
                .filter(existingUser -> !existingUser.getUserId().equals(userId))
                .findFirst()
                .ifPresent(u -> {
                    throw new ConflictException("Phone number already exists");
                });

        if (userDTO.getEmail() != null && !userRepository.existsByEmail(userDTO.getEmail())) {
            throw new ConflictException("Email already exists");
        }
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = RedisConfig.CACHE_USERS, key = "#userId"),
        @CacheEvict(value = RedisConfig.CACHE_USER_BY_NAME, allEntries = true)
    })
    public void deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public void disconnectUser(Long userId) {
        User user = getEntityByUserId(userId);
        user.setLastSeen(Instant.now());
        userRepository.save(user);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = RedisConfig.CACHE_USERS, key = "#updatePic.userId"),
        @CacheEvict(value = RedisConfig.CACHE_USER_BY_NAME, allEntries = true)
    })
    public void updateProfPic(updateProfilePic updatePic) {
        User user = getEntityByUserId(updatePic.getUserId());
        // Khớp với Postman: Nếu Postman gửi URL text, gán trực tiếp:
        // user.setProfilePicUrl(updatePic.getProfilePicUrl());

        // Nếu dùng file nhị phân (Blob):
        try {
            if (updatePic.getFile() != null && !updatePic.getFile().isEmpty()) {
                user.setProfilePicName(updatePic.getFile().getOriginalFilename());
                user.setImageType(updatePic.getFile().getContentType());
                user.setProfilePic(updatePic.getFile().getBytes());
                userRepository.save(user);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update profile picture", e);
        }
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = RedisConfig.CACHE_USERS, key = "#userBio.userId"),
        @CacheEvict(value = RedisConfig.CACHE_USER_BY_NAME, allEntries = true)
    })
    public void updateBio(updateBio userBio) {
        User user = getEntityByUserId(userBio.getUserId());
        user.setBio(userBio.getNewBio());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getEntityByUserId(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public boolean isExists(Long userId) {
        return userRepository.existsById(userId);
    }

    private UserResponse convertToUserResponse(User user) {
        String lastSeen = user.isOnline() ? "Online" : lastSeenHelper(user);
        return userMapper.toResponse(user, lastSeen);
    }

    private String lastSeenHelper(User user) {
        Instant lastSeen = user.getLastSeen();
        if (lastSeen == null)
            return "Never seen!";

        long minutesAgo = Duration.between(lastSeen, Instant.now()).toMinutes();
        if (minutesAgo < 5)
            return "last seen just now";
        if (minutesAgo < 60)
            return "last seen " + minutesAgo + " minutes ago";
        if (minutesAgo < 720)
            return "last seen " + (minutesAgo / 60) + " hour ago";

        if (minutesAgo < 1440) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());
            return "last seen at today " + formatter.format(lastSeen);
        }
        if (minutesAgo < 2880)
            return "last seen yesterday";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d 'at' hh:mm a")
                .withZone(ZoneId.systemDefault());
        return "last seen on " + formatter.format(lastSeen);
    }

    private void validateUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByPhoneNo(userDTO.getPhoneNo())) {
            throw new ConflictException("Phone number already exists");
        }
        if (userDTO.getEmail() != null && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new ConflictException("Email already exists");
        }
    }
}