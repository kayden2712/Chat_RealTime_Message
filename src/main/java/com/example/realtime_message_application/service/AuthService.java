package com.example.realtime_message_application.service;

import com.example.realtime_message_application.dto.auth.AuthenticationRequest;
import com.example.realtime_message_application.dto.auth.AuthenticationResponse;

public interface AuthService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
}
