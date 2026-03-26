package com.example.realtime_message_application.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    // Helper to get user principal
    private static CustomUserDetails getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("You're not authenticated");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof CustomUserDetails customUserDetails)) {
            throw new RuntimeException("Provide valid information to authenticate user");
        }

        return customUserDetails;
    }

    public static Long getUserId(){
        return getPrincipal().getUserId();
    }

    public static String getUsername(){
        return getPrincipal().getUsername();
    }

}
