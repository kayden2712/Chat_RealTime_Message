package com.example.realtime_message_application.mapper;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.realtime_message_application.dto.user.UserDTO;
import com.example.realtime_message_application.dto.user.UserResponse;
import com.example.realtime_message_application.model.User;

@Component
public class UserMapper {
    public UserResponse toResponse(User user, String lastSeen) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getPhoneNo(),
                user.getNickname(),
                user.getCreatedOn(),
                user.getBio(),
                user.isOnline() ? "Online" : "Offline",
                lastSeen);
    }

    public User applyUser(User target, UserDTO source) {
        target.setUsername(source.getUsername());
        target.setNickname(source.getNickname());
        target.setPhoneNo(source.getPhoneNo());
        target.setPassword(source.getPassword());

        String bio = source.getBio();
        target.setBio((bio == null || bio.isBlank()) ? "Hi, I'm new here" : bio);

        if (source.getImageFile() != null && !source.getImageFile().isEmpty()) {

            try {
                MultipartFile file = source.getImageFile();
                target.setProfilePic(file.getBytes());
                target.setImageType(file.getContentType());
                target.setProfilePicName(file.getOriginalFilename());
            } catch (Exception e) {
                throw new RuntimeException("Error while uploading profile pic: " + e.getMessage());
            }

        }

        return target;
    }
}
