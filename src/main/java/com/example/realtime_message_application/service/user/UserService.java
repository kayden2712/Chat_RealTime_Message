package com.example.realtime_message_application.service.user;

import java.util.List;

import com.example.realtime_message_application.dto.user.UserDTO;
import com.example.realtime_message_application.dto.user.UserResponse;
import com.example.realtime_message_application.dto.user.updateBio;
import com.example.realtime_message_application.dto.user.updateProfilePic;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long userId);

    UserResponse findByUsername(String username);

    List<UserResponse> findByPhoneNo(String phoneNo);

    List<UserResponse> findByKeyword(String keyword);

    UserResponse addUser(UserDTO user);

    UserResponse updateUser(Long userId, UserDTO user);

    void deleteUserById(Long userId);

    void disconnectUser(Long userId);

    void updateProfPic(updateProfilePic updatePic);

    void updateBio(updateBio userBio);

}
