package com.example.realtime_message_application.service.impl;

import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import com.example.realtime_message_application.dto.conversation.BlockingCommand;
import com.example.realtime_message_application.dto.conversation.BlockingDTO;
import com.example.realtime_message_application.model.Block;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.BlockRepository;
import com.example.realtime_message_application.service.BlockedService;
import com.example.realtime_message_application.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlockServerImpl implements BlockedService {

    private final BlockRepository blockRepository;
    private final UserService userService;

    @Override
    public List<Block> getBlockedList(Long blockerId) {

        if(!userService.isExists(blockerId)){
            throw new RuntimeException("Blocker ID is null.");
        }
        
        if(blockRepository.existsByBlockerId(blockerId)){
            throw new RuntimeException("Blocker ID is null.");
        }

        List<Block> blocks = blockRepository.findAllBlockedByBlocker(blockerId);
        
        return blocks;
    }

    // Dùng cho Admin hoặc các tác vụ hệ thống đã xác thực dữ liệu
    // Core Logic (Xử lý trực tiếp với Database)
    @Override
    public void blockUser(BlockingDTO blockingDTO) {
        if (blockingDTO.blockerId() == blockingDTO.blockedId()) {
            throw new RuntimeException("You can't block yourself.");
        }

        User blocker = userService.getEntityByUserId(blockingDTO.blockerId());
        User blocked = userService.getEntityByUserId(blockingDTO.blockedId());

        if (blockRepository.existsByBlockerAndBlocked(blockingDTO.blockerId(), blockingDTO.blockedId())) {
            throw new RuntimeException(
                    "User " + blocker.getNickname() + " already blocked " + blocked.getNickname() + ".");
        }

        Block block = Block.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();
        blockRepository.save(block);
    }

    @Override
    public void unblockUser(BlockingDTO blockingDTO) {
        if (blockingDTO.blockerId() == blockingDTO.blockedId()) {
            throw new RuntimeException("You can't block yourself.");
        }

        User blocker = userService.getEntityByUserId(blockingDTO.blockerId());
        User blocked = userService.getEntityByUserId(blockingDTO.blockedId());

        if (!blockRepository.existsByBlockerAndBlocked(blockingDTO.blockerId(), blockingDTO.blockedId())) {
            throw new RuntimeException(
                    "User " + blocker.getNickname() + "has not blocked " + blocked.getNickname() + ".");
        }

        blockRepository.deleteByBlockerAndBlocked(blockingDTO.blockerId(), blockingDTO.blockedId());
    }

    // Dùng cho các Controller (REST/WebSocket) để đảm bảo người dùng chỉ có thể
    // thực hiện trên chính họ
    // Security Wrapper (Xử lý bảo mật cho người dùng cuối)
    @Override
    public void blockUserForActor(BlockingDTO blockingDTO, Long actorId) {
        BlockingCommand command = new BlockingCommand(actorId, blockingDTO.blockedId());
        blockUser(new BlockingDTO(command.blockerId(), command.blockedId()));
    }

    @Override
    public void unblockUserForActor(BlockingDTO blockingDTO, Long actorId) {
        BlockingCommand command = new BlockingCommand(actorId, blockingDTO.blockedId());
        unblockUser(new BlockingDTO(command.blockerId(), command.blockedId()));
    }

}
