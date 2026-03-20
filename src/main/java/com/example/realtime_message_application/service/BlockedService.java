package com.example.realtime_message_application.service;

import java.util.List;

import com.example.realtime_message_application.dto.conversation.BlockingDTO;
import com.example.realtime_message_application.model.Block;

public interface BlockedService {

    void blockUser(BlockingDTO blockingDTO);

    void unblockUser(BlockingDTO blockingDTO);

    void blockUserForActor(BlockingDTO blockingDTO, Long actorId);

    void unblockUserForActor(BlockingDTO blockingDTO, Long actorId);

    List<Block> getBlockedList(Long blockerId);
}