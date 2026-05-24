package com.example.realtime_message_application.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.realtime_message_application.enums.ConversationType;
import com.example.realtime_message_application.enums.MessageType;
import com.example.realtime_message_application.enums.ParticipantRole;
import com.example.realtime_message_application.model.Conversation;
import com.example.realtime_message_application.model.ConversationParticipant;
import com.example.realtime_message_application.model.Message;
import com.example.realtime_message_application.model.User;
import com.example.realtime_message_application.repository.ConversationRepository;
import com.example.realtime_message_application.repository.MessageRepository;
import com.example.realtime_message_application.repository.ParticipantRepository;
import com.example.realtime_message_application.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestDataService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ParticipantRepository participantRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    private final String[] firstNames = {
            "Nguyen", "Tran", "Pham", "Hoang", "Vu", "Dang", "Bui", "Ly", "Ngo", "Do",
            "Vo", "Ho", "Phan", "Duong", "Thai"
    };

    private final String[] lastNames = {
            "An", "Duc", "Linh", "Hung", "Minh", "Hoa", "Tung", "Thanh", "Khanh", "Long",
            "Phong", "Khoa", "Nhan", "Tam", "Quyen"
    };

    private final String[] contents = {
            "Xin chào! Bạn khỏe không?",
            "Tôi đang làm việc trên project mới. Rất hứng thú!",
            "Bạn có thời gian gặp mặt không?",
            "Đã xem tin nhắn chưa?",
            "Cảm ơn bạn rất nhiều!",
            "Mình trao đổi lúc nào được?",
            "Cuối tuần đi chơi không?",
            "Công việc hôm nay rất bận rộn",
            "Mình gửi file cho bạn rồi",
            "Ok, sẽ liên hệ lại sau ạ",
            "Great work on the project!",
            "Let's catch up later",
            "Thanks for the update",
            "See you tomorrow!",
            "Have a great day!",
            "Tôi hoàn thành task rồi",
            "Bạn đã review code chưa?",
            "Meeting lúc 2h chiều nhé",
            "Đừng quên deadline ngày mai",
            "Ai có ý kiến gì không?",
            "Tuyệt vời! Cảm ơn bạn",
            "Bạn làm thế nào vậy?",
            "Hôm nay thời tiết đẹp quá",
            "Tôi cần hỗ trợ về phần này",
            "Bạn vui vẻ lên nhé",
            "Chúc bạn may mắn!",
            "Tôi sẽ gửi chi tiết sau",
            "Bạn còn thắc mắc gì không?",
            "Tất cả đều ổn?",
            "Liên hệ tôi nếu cần gì",
            "No problem at all",
            "That sounds good",
            "Perfect timing!",
            "I'll help you with that",
            "Can you send me the file?",
            "I'll check it later",
            "Please review this",
            "Let me know if you need anything",
            "Good job!",
            "Amazing work!",
            "Keep it up!",
            "You are doing great!",
            "Let's finish this today",
            "Do you have any updates?",
            "I will join the meeting",
            "See you soon",
            "Take care",
            "I appreciate your help",
            "Thank you!",
            "Please confirm once done",
            "This is very important",
            "Let's talk later",
            "I'm on my way",
            "Running a little late",
            "I need more information",
            "Let's start now",
            "We can discuss this tomorrow",
            "I have a question about this",
            "Is this okay for you?",
            "Please send the details",
            "I will share the report soon",
            "The plan looks good",
            "I'm happy with the outcome"
    };

    private final String[] conversationTitles = {
            "Dev Team", "Marketing Group", "Sales Team", "Project Alpha",
            "Brainstorming", "Daily Standup", "Coffee Chat", "Weekend Plans",
            "General Discussion", "Tech News", "Random Chat", "Work Updates"
    };

    @Transactional
    public SeedResult seedRandomData(int userCount, int conversationCount, int messagesPerUser) {
        participantRepository.deleteAll();
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        userRepository.deleteAll();

        List<User> users = createUsers(userCount);
        List<Conversation> conversations = createConversations(users, conversationCount);
        int totalMessages = createMessages(users, conversations, messagesPerUser);

        return new SeedResult(users.size(), conversations.size(), totalMessages);
    }

    private List<User> createUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            String first = firstNames[random.nextInt(firstNames.length)];
            String last = lastNames[random.nextInt(lastNames.length)];
            String username = first.toLowerCase() + last.toLowerCase() + System.currentTimeMillis() % 10000 + random.nextInt(100);
            String email = username + "@example.com";
            String phoneNo = "+849" + String.format("%08d", random.nextInt(100000000));

            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode("password123"))
                    .email(email)
                    .phoneNo(phoneNo)
                    .nickname(first + " " + last)
                    .bio("Random seeded user for testing")
                    .createdOn(Instant.now().minus(random.nextInt(30), ChronoUnit.DAYS))
                    .online(random.nextBoolean())
                    .lastSeen(Instant.now().minus(random.nextInt(24), ChronoUnit.HOURS))
                    .build();

            users.add(userRepository.save(user));
        }
        return users;
    }

    private List<Conversation> createConversations(List<User> users, int count) {
        List<Conversation> result = new ArrayList<>();
        int groupCount = Math.max(1, count / 2);
        int privateCount = Math.max(1, count - groupCount);

        for (int i = 0; i < groupCount; i++) {
            String title = conversationTitles[random.nextInt(conversationTitles.length)] + " Group " + (i + 1);
            Conversation conv = Conversation.builder()
                    .type(ConversationType.GROUP)
                    .title(title)
                    .description("Auto generated group conversation")
                    .creator(users.get(random.nextInt(users.size())))
                    .createdAt(Instant.now().minus(random.nextInt(60), ChronoUnit.DAYS))
                    .participants(new HashSet<>())
                    .build();

            int participantsCount = 5 + random.nextInt(Math.min(4, users.size() - 4));
            Set<ConversationParticipant> participants = new HashSet<>();
            Set<Integer> chosen = new HashSet<>();
            while (chosen.size() < participantsCount) {
                chosen.add(random.nextInt(users.size()));
            }
            int idx = 0;
            for (Integer userIndex : chosen) {
                User participant = users.get(userIndex);
                participants.add(ConversationParticipant.builder()
                        .conversation(conv)
                        .user(participant)
                        .participantRole(idx == 0 ? ParticipantRole.ADMIN : ParticipantRole.MEMBER)
                        .joinedOn(Instant.now().minus(random.nextInt(60), ChronoUnit.DAYS))
                        .isMuted(random.nextDouble() < 0.2)
                        .isArchived(random.nextDouble() < 0.1)
                        .isFavorite(random.nextDouble() < 0.3)
                        .build());
                idx++;
            }
            conv.setParticipants(participants);
            result.add(conversationRepository.save(conv));
        }

        for (int i = 0; i < privateCount; i++) {
            User user1 = users.get(random.nextInt(users.size()));
            User user2 = users.get(random.nextInt(users.size()));
            if (user1.equals(user2)) {
                i--;
                continue;
            }
            Conversation conv = Conversation.builder()
                    .type(ConversationType.PRIVATE)
                    .title(user1.getNickname() + " - " + user2.getNickname())
                    .description("Auto generated private conversation")
                    .creator(user1)
                    .createdAt(Instant.now().minus(random.nextInt(60), ChronoUnit.DAYS))
                    .participants(new HashSet<>())
                    .build();

            Set<ConversationParticipant> participants = new HashSet<>();
            participants.add(ConversationParticipant.builder()
                    .conversation(conv)
                    .user(user1)
                    .participantRole(ParticipantRole.MEMBER)
                    .joinedOn(conv.getCreatedAt())
                    .build());
            participants.add(ConversationParticipant.builder()
                    .conversation(conv)
                    .user(user2)
                    .participantRole(ParticipantRole.MEMBER)
                    .joinedOn(conv.getCreatedAt())
                    .build());
            conv.setParticipants(participants);
            result.add(conversationRepository.save(conv));
        }

        return result;
    }

    private int createMessages(List<User> users, List<Conversation> conversations, int messagesPerUser) {
        int messageCount = 0;
        for (User user : users) {
            List<Conversation> available = new ArrayList<>();
            for (Conversation conversation : conversations) {
                if (conversation.getParticipants().stream().anyMatch(cp -> cp.getUser().getUserId().equals(user.getUserId()))) {
                    available.add(conversation);
                }
            }
            if (available.isEmpty()) {
                available.add(conversations.get(random.nextInt(conversations.size())));
            }
            for (int i = 0; i < messagesPerUser; i++) {
                Conversation conv = available.get(random.nextInt(available.size()));
                String content = contents[random.nextInt(contents.length)];
                if (random.nextDouble() < 0.25) {
                    content += " " + contents[random.nextInt(contents.length)];
                }
                Message message = Message.builder()
                        .conversation(conv)
                        .sender(user)
                        .content(content)
                        .messageType(MessageType.TEXT)
                        .pinned(random.nextDouble() < 0.03)
                        .isDeleted(false)
                        .createdAt(Instant.now().minus(random.nextInt(60), ChronoUnit.DAYS)
                                .plus(random.nextInt(1440), ChronoUnit.MINUTES))
                        .build();
                messageRepository.save(message);
                messageCount++;
            }
        }
        return messageCount;
    }

    @RequiredArgsConstructor
    public static class SeedResult {
        private final int usersCreated;
        private final int conversationsCreated;
        private final int messagesCreated;

        public int getUsersCreated() {
            return usersCreated;
        }

        public int getConversationsCreated() {
            return conversationsCreated;
        }

        public int getMessagesCreated() {
            return messagesCreated;
        }
    }
}
