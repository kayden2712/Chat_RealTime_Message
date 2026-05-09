package com.example.realtime_message_application.service;

import java.util.List;
import java.util.Map;

public interface PresenceService {

    boolean isOnline(Long userId);

    Map<Long, String> getFriendsStatus(List<Long> friendIds);

    List<Long> getOnlineUserByConvId(Long convId);

    void markOnline(Long userId);

    void markOffline(Long userId);

    void refreshOnline(Long userId);
}
