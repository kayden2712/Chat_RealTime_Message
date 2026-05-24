// package com.example.realtime_message_application.config;

// import java.time.Instant;
// import java.time.temporal.ChronoUnit;
// import java.util.ArrayList;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Random;
// import java.util.Set;

// import org.springframework.boot.CommandLineRunner;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Component;

// import com.example.realtime_message_application.enums.ConversationType;
// import com.example.realtime_message_application.enums.MessageType;
// import com.example.realtime_message_application.enums.ParticipantRole;
// import com.example.realtime_message_application.model.Conversation;
// import com.example.realtime_message_application.model.ConversationParticipant;
// import com.example.realtime_message_application.model.Message;
// import com.example.realtime_message_application.model.User;
// import com.example.realtime_message_application.repository.ConversationRepository;
// import com.example.realtime_message_application.repository.MessageRepository;
// import com.example.realtime_message_application.repository.UserRepository;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// @Component
// @RequiredArgsConstructor
// @Slf4j
// public class DataSeeder implements CommandLineRunner {

//     private final UserRepository userRepository;
//     private final ConversationRepository conversationRepository;
//     private final MessageRepository messageRepository;
//     private final PasswordEncoder passwordEncoder;

//     private final Random random = new Random();

//     private final String[] firstNames = {
//             "Nguyễn", "Trần", "Phạm", "Hoàng", "Vũ", "Đặng", "Bùi", "Lý", "Ngô", "Đỗ",
//             "Võ", "Hồ", "Phan", "Dương", "Thái"
//     };

//     private final String[] lastNames = {
//             "An", "Duc", "Linh", "Hung", "Minh", "Hoa", "Tung", "Thanh", "Khanh", "Long",
//             "Phong", "Khoa", "Nhân", "Tâm", "Quyền"
//     };

//     private final String[] sampleMessages = {
//             "Xin chào! Bạn khỏe không?",
//             "Tôi đang làm việc trên project mới. Rất hứng thú!",
//             "Bạn có thời gian gặp mặt không?",
//             "Đã xem tin nhắn chưa?",
//             "Cảm ơn bạn rất nhiều!",
//             "Mình trao đổi lúc nào được?",
//             "Cuối tuần đi chơi không?",
//             "Công việc hôm nay rất bận rộn",
//             "Mình gửi file cho bạn rồi",
//             "Ok, sẽ liên hệ lại sau ạ",
//             "Great work on the project!",
//             "Let's catch up later",
//             "Thanks for the update",
//             "See you tomorrow!",
//             "Have a great day!",
//             "Tôi hoàn thành task rồi",
//             "Bạn đã review code chưa?",
//             "Meeting lúc 2h chiều nhé",
//             "Đừng quên deadline ngày mai",
//             "Ai có ý kiến gì không?",
//             "Tuyệt vời! Cảm ơn bạn",
//             "Bạn làm thế nào vậy?",
//             "Hôm nay thời tiết đẹp quá",
//             "Tôi cần hỗ trợ về phần này",
//             "Bạn vui vẻ lên nhé",
//             "Chúc bạn may mắn!",
//             "Tôi sẽ gửi chi tiết sau",
//             "Bạn còn thương tiếc gì không?",
//             "Tất cả đều ổn?",
//             "Liên hệ tôi nếu cần gì",
//             "I love this project!",
//             "Great idea! Let me think about it",
//             "Do you have any feedback?",
//             "I agree with you",
//             "Let's discuss this later",
//             "Can you send me the file?",
//             "I'll help you with that",
//             "No problem at all",
//             "That sounds good",
//             "Perfect timing!",
//             "Tôi sẽ check ngay",
//             "Bạn có free không?",
//             "Đợi mình tí nhé",
//             "Mình đang bận chút xíu",
//             "Lúc nào có thời gian?",
//             "Cơm chưa bạn?",
//             "Đi cafe không?",
//             "Hôm nay bận quá",
//             "Mệt quá đi",
//             "Cần hỗ trợ gì không?",
//             "Mình xong rồi đó",
//             "Tạm biệt bạn!",
//             "Hẹn gặp lại!",
//             "Chuyên gia phần này là ai?",
//             "Bạn hiểu rồi chứ?",
//             "Rõ ràng lắm",
//             "Mình không hiểu lắm",
//             "Có thể giải thích lại được không?",
//             "Bạn có làm được không?",
//             "Tôi làm được rồi",
//             "Tôi chưa làm được",
//             "Cần thêm thời gian",
//             "Có thể hoàn thành hôm nay",
//             "Good morning everyone!",
//             "Good night!",
//             "See you soon!",
//             "Talk to you later!",
//             "Take care!",
//             "Keep up the good work!",
//             "You're doing great!",
//             "Well done!",
//             "Awesome job!",
//             "Incredible!",
//             "Amazing!",
//             "Wonderful!",
//             "Fantastic!",
//             "Excellent work!",
//             "Outstanding!",
//             "Bạn là super star",
//             "Tuyệt diệu!",
//             "Quá hay!",
//             "Không ai tốt bằng bạn",
//             "Bạn thật pro!",
//             "Bạn giỏi quá!",
//             "Mình kính phục bạn",
//             "Bạn là mẫu mực",
//             "Học hỏi mãi mãi",
//             "Bạn là inspiration",
//             "Tôi thích làm việc với bạn",
//             "Cơm xong chưa?",
//             "Ăn cơm chưa bạn?",
//             "Tối nay ăn gì?",
//             "Bạn có kế hoạch gì không?",
//             "Sáng mai gặp nhé",
//             "Mình sẽ gọi bạn",
//             "Nhắn tin cho mình",
//             "Bạn ở đâu?",
//             "Tôi ở văn phòng",
//             "Tôi đang ở nhà",
//             "Bạn ở đâu đây?",
//             "I'm at the office",
//             "I'm working from home",
//             "I'm in a meeting",
//             "I'll call you back",
//             "Can you call me?",
//             "Let me call you",
//             "Voice call?",
//             "Video call?",
//             "Just text me",
//             "Mình gọi video được không?",
//             "Có điều gì sai sao?",
//             "Mọi thứ đều tốt",
//             "Không sao cả",
//             "Đó là vấn đề lớn",
//             "Tôi lo lắm",
//             "Yên tâm đi",
//             "Mọi thứ sẽ ổn",
//             "Bạn lo gì nữa",
//             "Hãy tự tin",
//             "Bạn sẽ thành công",
//             "Không bao giờ từ bỏ",
//             "Hãy cố gắng hơn",
//             "Bạn có thể làm được",
//             "Tôi tin tưởng bạn"
//     };

//     private final String[] conversationTitles = {
//             "Dev Team", "Marketing Group", "Sales Team", "Project Alpha",
//             "Brainstorming Ideas", "Daily Standup", "Coffee Chat", "Weekend Plans",
//             "General Discussion", "Tech News", "Random Chat", "Work Updates",
//             "Team Building", "Backend Team", "Frontend Team", "QA Team",
//             "HR Discussion", "Finance Talk", "Product Meeting", "Planning Session"
//     };

//     @Override
//     public void run(String... args) throws Exception {
//         log.info("🌱 Starting Data Seeding với 100+ tin nhắn cho mỗi user...");

//         try {
//             // Clear existing data
//             messageRepository.deleteAll();
//             conversationRepository.deleteAll();
//             userRepository.deleteAll();

//             // Create sample users
//             List<User> users = createSampleUsers();
//             log.info("✅ Tạo {} người dùng", users.size());

//             // Create conversations with participants
//             List<Conversation> conversations = createSampleConversations(users);
//             log.info("✅ Tạo {} cuộc trò chuyện", conversations.size());

//             // Create messages (mỗi user sẽ có ~100+ tin nhắn)
//             createSampleMessages(conversations);
//             log.info("✅ Tạo tin nhắn xong");

//             long totalMessages = messageRepository.count();
//             log.info("🎉 Data Seeding Hoàn Thành! Tổng cộng {} tin nhắn", totalMessages);
//         } catch (Exception e) {
//             log.error("❌ Lỗi khi seeding data", e);
//         }
//     }

//     private List<User> createSampleUsers() {
//         List<User> users = new ArrayList<>();

//         for (int i = 0; i < 10; i++) {
//             String firstName = firstNames[random.nextInt(firstNames.length)];
//             String lastName = lastNames[random.nextInt(lastNames.length)];
//             String username = "user" + (i + 1) + "_" + System.currentTimeMillis();
//             String email = "user" + (i + 1) + "@example.com";
//             String phoneNo = "+84" + String.format("%08d", 900000000 + i * 10000000 + random.nextInt(1000000));

//             User user = User.builder()
//                     .username(username)
//                     .password(passwordEncoder.encode("password123"))
//                     .email(email)
//                     .phoneNo(phoneNo)
//                     .nickname(firstName + " " + lastName)
//                     .bio("Software Developer - Passionate about coding")
//                     .createdOn(Instant.now().minus(random.nextInt(30), ChronoUnit.DAYS))
//                     .online(random.nextBoolean())
//                     .lastSeen(Instant.now().minus(random.nextInt(24), ChronoUnit.HOURS))
//                     .build();

//             users.add(userRepository.save(user));
//         }

//         return users;
//     }

//     private List<Conversation> createSampleConversations(List<User> users) {
//         List<Conversation> conversations = new ArrayList<>();

//         // Create group conversations
//         for (int i = 0; i < 5; i++) {
//             String title = conversationTitles[random.nextInt(conversationTitles.length)];
//             User creator = users.get(random.nextInt(users.size()));

//             Conversation conversation = Conversation.builder()
//                     .type(ConversationType.GROUP)
//                     .title(title + " #" + (i + 1))
//                     .description("Group for " + title)
//                     .creator(creator)
//                     .createdAt(Instant.now().minus(random.nextInt(60), ChronoUnit.DAYS))
//                     .participants(new HashSet<>())
//                     .build();

//             // Add random participants (6-8 people per group)
//             Set<ConversationParticipant> participants = new HashSet<>();
//             int participantCount = 6 + random.nextInt(3);

//             for (int j = 0; j < participantCount && j < users.size(); j++) {
//                 User participant = users.get(j);
//                 ConversationParticipant cp = ConversationParticipant.builder()
//                         .conversation(conversation)
//                         .user(participant)
//                         .participantRole(j == 0 ? ParticipantRole.ADMIN : ParticipantRole.MEMBER)
//                         .joinedOn(Instant.now().minus(random.nextInt(60), ChronoUnit.DAYS))
//                         .isMuted(random.nextDouble() < 0.2)
//                         .isArchived(random.nextDouble() < 0.1)
//                         .isFavorite(random.nextDouble() < 0.3)
//                         .build();
//                 participants.add(cp);
//             }

//             conversation.setParticipants(participants);
//             conversations.add(conversationRepository.save(conversation));
//         }

//         // Create private conversations (1-1)
//         for (int i = 0; i < 5; i++) {
//             User user1 = users.get(i);
//             User user2 = users.get((i + 1) % users.size());

//             if (!user1.equals(user2)) {
//                 String title = user1.getNickname() + " - " + user2.getNickname();
//                 Conversation conversation = Conversation.builder()
//                         .type(ConversationType.PRIVATE)
//                         .title(title)
//                         .creator(user1)
//                         .createdAt(Instant.now().minus(random.nextInt(90), ChronoUnit.DAYS))
//                         .participants(new HashSet<>())
//                         .build();

//                 // Add both participants
//                 Set<ConversationParticipant> participants = new HashSet<>();
//                 participants.add(ConversationParticipant.builder()
//                         .conversation(conversation)
//                         .user(user1)
//                         .participantRole(ParticipantRole.MEMBER)
//                         .joinedOn(conversation.getCreatedAt())
//                         .build());
//                 participants.add(ConversationParticipant.builder()
//                         .conversation(conversation)
//                         .user(user2)
//                         .participantRole(ParticipantRole.MEMBER)
//                         .joinedOn(conversation.getCreatedAt())
//                         .build());

//                 conversation.setParticipants(participants);
//                 conversations.add(conversationRepository.save(conversation));
//             }
//         }

//         return conversations;
//     }

//     private void createSampleMessages(List<Conversation> conversations) {
//         int totalMessageCount = 0;

//         for (Conversation conversation : conversations) {
//             List<ConversationParticipant> participants = new ArrayList<>(conversation.getParticipants());
//             if (participants.isEmpty())
//                 continue;

//             // Mỗi conversation sẽ có ~100+ tin nhắn
//             int messageCountPerConv = 100 + random.nextInt(50);

//             for (int i = 0; i < messageCountPerConv; i++) {
//                 // Random sender từ participants
//                 User sender = participants.get(random.nextInt(participants.size())).getUser();
//                 String content = sampleMessages[random.nextInt(sampleMessages.length)];

//                 // Thêm variation bằng cách combine messages
//                 if (random.nextDouble() < 0.3) {
//                     content = content + " " + sampleMessages[random.nextInt(sampleMessages.length)];
//                 }

//                 Message message = Message.builder()
//                         .content(content)
//                         .conversation(conversation)
//                         .sender(sender)
//                         .messageType(MessageType.TEXT)
//                         .pinned(random.nextDouble() < 0.05)
//                         .isDeleted(random.nextDouble() < 0.02)
//                         .createdAt(Instant.now()
//                                 .minus(random.nextInt(60), ChronoUnit.DAYS)
//                                 .plus(i * 10, ChronoUnit.MINUTES)
//                                 .plus(random.nextInt(3600), ChronoUnit.SECONDS))
//                         .build();

//                 messageRepository.save(message);
//                 totalMessageCount++;
//             }
//         }

//         log.info("📊 Tạo tổng cộng {} tin nhắn", totalMessageCount);
//     }
// }
