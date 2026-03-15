package com.example.realtime_message_application.dto.user;

public record updateBio(Long userId, String newBio) {
    public Long getUserId(){return userId;}
    public String getNewBio(){return newBio;}
}
