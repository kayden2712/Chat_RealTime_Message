# RealTime Message Application

A real-time messaging backend built with **Spring Boot 3.5**, **WebSocket (STOMP)**, **PostgreSQL**, **Redis**, and **Firebase Cloud Messaging (FCM)**. Supports private and group conversations with comprehensive messaging features like message pinning, read receipts, user blocking, muting, and distributed rate limiting.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Key Features](#key-features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [WebSocket/STOMP Guide](#websocketstomp-guide)
- [Project Structure](#project-structure)
- [Database Models](#database-models)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)

---

## Overview

This is a production-ready real-time messaging backend that exposes both **REST APIs** for management operations and **WebSocket (STOMP)** endpoints for real-time message exchange. The application handles:

- **1-on-1 private conversations** and **group chats**
- **Real-time message delivery** via WebSocket
- **Message management** (edit, delete, pin)
- **User presence tracking** and online status
- **Advanced security** with JWT authentication and rate limiting
- **Push notifications** via Firebase Cloud Messaging
- **Distributed caching** and pub/sub via Redis

---

## Tech Stack

| Category          | Technology                                |
| :---------------- | :---------------------------------------- |
| **Framework**      | Spring Boot 3.5.11, Spring Security      |
| **Language**       | Java 21                                   |
| **Real-time**      | WebSocket (STOMP over SockJS)             |
| **Database**       | PostgreSQL 18                             |
| **Cache / Pub-Sub**| Redis + Redisson 3.52.0                   |
| **Push Notif.**    | Firebase Admin SDK 9.7.0                  |
| **Rate Limiting**  | Bucket4j 8.14.0 (Redis-backed)            |
| **Authentication** | JWT (jjwt 0.13.0)                        |
| **Build**          | Maven (with `mvnw` wrapper)               |
| **Containerization**| Docker & Docker Compose                   |

---

## Key Features

### 💬 Messaging
- ✅ **Real-time chat** over WebSocket for both private and group conversations
- ✅ **Multiple message types**: TEXT, IMAGE, FILE
- ✅ **Message actions**: Edit, soft-delete, restore, and pin/unpin
- ✅ **Read receipts**: Track message read status per user
- ✅ **Metadata support**: Store custom data with messages

### 👥 Conversations
- ✅ **Private** (1-on-1) and **Group** conversation types
- ✅ **Participant management**: Add, remove, leave groups
- ✅ **Roles**: ADMIN and MEMBER with permission-based operations
- ✅ **Conversation controls**: Mute, archive, mark as favorite
- ✅ **Customization**: Update title, description, and group image

### 👤 User Management
- ✅ **User profiles** with bio and profile pictures
- ✅ **Blocking**: Block/unblock users to prevent communication
- ✅ **Moderation**: Ban/unban users from group conversations
- ✅ **Presence tracking**: Real-time online/offline status
- ✅ **User search**: Find users by keyword, username, or phone

### 🔒 Security & Infrastructure
- ✅ **JWT authentication** with access and refresh tokens
- ✅ **OAuth2 Resource Server** support
- ✅ **Distributed rate limiting** via Bucket4j + Redis
- ✅ **Push notifications** via Firebase Cloud Messaging
- ✅ **Scheduled maintenance** tasks for data cleanup

---

## Prerequisites

- **Docker & Docker Compose** (for containerized setup)
- **Java 21** (for local development)
- **PostgreSQL 18** (included in docker-compose)
- **Redis** (included in docker-compose)
- **Firebase Service Account** JSON (optional, for FCM features)

---

## Quick Start

### Option 1: Docker Compose (Recommended)

1. **Create `.env` file** in project root:
```bash
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgrespass
POSTGRES_DB=realtimedb
REDIS_PORT=6379
JWT_SECRET_KEY=your_super_secret_jwt_key_here
```

2. **Start all services**:
```bash
docker compose -f docker-compose.postgreSQL.yml --env-file .env up --build -d
```

3. **Verify services are running**:
```bash
docker ps
docker logs -f realtimemessage-service
```

4. **Access the application**:
   - API: `http://localhost:8080`
   - Health check: `GET http://localhost:8080/actuator/health`

5. **Stop services**:
```bash
docker compose -f docker-compose.postgreSQL.yml down
```

### Option 2: Local Development

1. **Ensure PostgreSQL and Redis are running**:
```bash
# Start PostgreSQL (if not running)
docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=postgrespass postgres:18

# Start Redis (if not running)
docker run -d -p 6379:6379 redis:latest
```

2. **Configure application.properties**:
   - Copy and update `src/main/resources/application.properties` with your local DB credentials

3. **Run the application**:
```bash
./mvnw spring-boot:run
```

4. **Application will start** at `http://localhost:8080`

---

## Configuration

### Environment Variables

| Variable                      | Example                              | Description                        |
| ----------------------------- | ------------------------------------ | ---------------------------------- |
| `SPRING_DATASOURCE_URL`       | `jdbc:postgresql://localhost:5432/realtimedb` | Database connection URL |
| `SPRING_DATASOURCE_USERNAME`  | `postgres`                           | Database username                  |
| `SPRING_DATASOURCE_PASSWORD`  | `postgrespass`                       | Database password                  |
| `SPRING_DATA_REDIS_HOST`      | `localhost`                          | Redis server host                  |
| `SPRING_DATA_REDIS_PORT`      | `6379`                               | Redis server port                  |
| `JWT_SECRET_KEY`              | `your_secret_key`                    | JWT signing secret (min 256 bits)  |
| `APP_FIREBASE_CONFIGURATION_FILE` | `serviceAccountKey.json`         | Firebase service account path      |

### Docker Compose Service Names

When running with Docker Compose, use these service names:
- **Database**: `db-postgres` (port 5432)
- **Redis**: `redis-cache` (port 6379)
- **Application**: `realtimemessage-service` (port 8080)

---

## API Documentation

### Authentication Endpoints

**Register User**
```bash
POST /api/v1/user/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securepass123",
  "phoneNo": "9876543210",
  "nickname": "John Doe"
}

Response: "User registered successfully with ID: 1"
```

**Login**
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "securepass123"
}

Response:
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "expiresIn": 3600
}
```

### User Endpoints (Examples)

```bash
# Get all users (Admin only)
GET /api/v1/user
Authorization: Bearer <ACCESS_TOKEN>

# Get user by ID (Admin only)
GET /api/v1/user/{userId}
Authorization: Bearer <ACCESS_TOKEN>

# Find user by username
GET /api/v1/user/username/{username}

# Find users by keyword
GET /api/v1/user/keyword/{keyword}

# Update user
PUT /api/v1/user/update/{userId}
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

# Delete user
DELETE /api/v1/user/{userId}
Authorization: Bearer <ACCESS_TOKEN>

# Update user bio
PUT /api/v1/user/update/bio
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
{
  "userId": 1,
  "bio": "Software Developer 🚀"
}
```

### Firebase Cloud Messaging (FCM)

```bash
# Register FCM token for push notifications
POST /api/v1/fcm/register
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "userId": 1,
  "token": "device_fcm_token_here"
}

# Unregister FCM token
DELETE /api/v1/fcm/unregister?token=device_fcm_token_here
Authorization: Bearer <ACCESS_TOKEN>
```

---

## WebSocket/STOMP Guide

### Connection

**WebSocket Endpoint**: `/ws`

**Connection Headers**:
```
Authorization: Bearer <JWT_TOKEN>
accept-version: 1.2
```

### Subscribe to Conversations

```javascript
// Subscribe to group conversation messages
client.subscribe('/topic/conversations.123', function(message) {
  console.log('Group message:', JSON.parse(message.body));
});

// Subscribe to private conversation messages
client.subscribe('/user/queue/conversation.123', function(message) {
  console.log('Private message:', JSON.parse(message.body));
});
```

### Send Messages

```javascript
// Send message to conversation
client.send('/app/chat.sendMessage', {}, JSON.stringify({
  conversationId: 123,
  type: 'TEXT',
  content: 'Hello, everyone!',
  senderId: 1,
  metadata: {}
}));

// Edit message
client.send('/app/chat.editMessage', {}, JSON.stringify({
  messageId: 456,
  content: 'updated content'
}));

// Delete message
client.send('/app/chat.deleteMessage', {}, JSON.stringify({
  messageId: 456
}));
```

### STOMP Destinations

| Destination                          | Type    | Description                           |
| :----------------------------------- | :------ | :------------------------------------ |
| `/topic/conversations.{convId}`      | Topic   | Broadcast to all users in group       |
| `/user/queue/conversation.{convId}`  | Queue   | Private message queue per user        |
| `/app/chat.sendMessage`              | App     | Send a new message                    |
| `/app/chat.editMessage`              | App     | Edit an existing message              |
| `/app/chat.deleteMessage`            | App     | Soft-delete a message                 |
| `/app/chat.block`                    | App     | Block a user                          |
| `/app/chat.unblock`                  | App     | Unblock a user                        |

---

## Project Structure

```
src/main/java/com/example/realtime_message_application/
├── RealtimemessageApplication.java          # Main Spring Boot app
├── component/
│   ├── ChatEventPublisher.java              # WebSocket message broadcasting
│   ├── FCMInitializer.java                  # Firebase initialization
│   ├── RateLimitingFilter.java              # Rate limiting filter
│   ├── RateLimitingInterceptor.java         # HTTP interceptor
│   ├── RedisMessageSubscriber.java          # Redis pub/sub subscriber
│   ├── RedisNotificationSubscriber.java     # Notification subscriber
│   ├── UserInterceptor.java                 # User context resolver
│   └── WebSocketEventListener.java          # Presence tracking
├── config/
│   ├── DataSeeder.java                      # Sample data initialization
│   ├── RedisConfig.java                     # Redis/Redisson config
│   ├── SecurityConfig.java                  # Spring Security setup
│   ├── WebConfig.java                       # Web MVC configuration
│   └── WebsocketConfig.java                 # STOMP/WebSocket setup
├── controller/                              # REST API controllers
├── dto/                                     # Request/Response DTOs
│   ├── auth/
│   ├── conversation/
│   ├── message/
│   ├── notification/
│   └── user/
├── enums/
│   ├── ConversationType.java                # PRIVATE, GROUP
│   ├── MessageType.java                     # TEXT, IMAGE, FILE
│   └── ParticipantRole.java                 # ADMIN, MEMBER
├── exception/                               # Custom exception classes
├── mapper/                                  # Entity/DTO mapping
├── model/                                   # JPA entity classes
├── repository/                              # Spring Data JPA repos
├── security/
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtHandshakeInterceptor.java
│   ├── JwtService.java
│   └── SecurityUtils.java
└── service/                                 # Business logic layer
    └── impl/                                # Service implementations
```

---

## Database Models

| Entity                    | Table                       | Description                              |
| :------------------------ | :-------------------------- | :--------------------------------------- |
| `User`                    | `users`                     | User account with profile information    |
| `Conversation`            | `conversations`             | PRIVATE or GROUP conversation            |
| `ConversationParticipant` | `conversation_participants` | User participation with roles            |
| `Message`                 | `messages`                  | Chat message with soft-delete support    |
| `ReadReceipt`             | `read_receipts`             | Per-user read status tracking            |
| `Block`                   | `blocks`                    | User blocking relationships              |
| `BannedUser`              | `banned_users`              | Banned members in groups                 |
| `FCMToken`                | `fcm_tokens`                | Firebase device tokens                   |
| `Mute`                    | `mutes`                     | Muted conversations per user             |

---

## Testing

### Run Unit and Integration Tests

```bash
./mvnw test
```

### Run Specific Test Class

```bash
./mvnw test -Dtest=UserServiceImplTest
```

### Run Tests with Coverage

```bash
./mvnw test jacoco:report
# View coverage report at target/site/jacoco/index.html
```

---

## Building for Production

### Build JAR

```bash
./mvnw clean package -DskipTests
java -jar target/realtimemessage-*.jar
```

### Build Docker Image

```bash
docker build -t my-registry/realtimemessage:1.0 .
docker push my-registry/realtimemessage:1.0
```

### Run with Docker (without Compose)

```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/realtimedb \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgrespass \
  -e SPRING_DATA_REDIS_HOST=host.docker.internal \
  -e JWT_SECRET_KEY=your_secret \
  --name realtimemessage \
  realtimemessage:latest
```

---

## Troubleshooting

| Problem | Solution |
| :------ | :-------- |
| **Database connection failed** | Verify `SPRING_DATASOURCE_URL`, check PostgreSQL is running: `docker logs db-postgres` |
| **Redis connection errors** | Check Redis container: `docker logs redis-cache`, ensure port 6379 is accessible |
| **FCM initialization failed** | Validate `serviceAccountKey.json` exists in `src/main/resources` |
| **Rate limiting not working** | Verify Redis is running and `RedisConfig.java` is properly configured |
| **WebSocket connection timeout** | Ensure JWT token is valid and passed in connection headers |
| **Message not delivered** | Check user is subscribed to correct topic; verify `ChatEventPublisher.java` |
| **Port 8080 already in use** | Kill the process: `lsof -i :8080 \| grep LISTEN \| awk '{print $2}' \| xargs kill -9` |

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m "Add amazing feature"`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request with detailed description

---

## License

This project is provided as a demo and educational reference. Modify and distribute as needed.

---

## Project Structure

```
src/main/java/com/example/realtime_message_application/
├── RealtimemessageApplication.java    # Main entry point
├── component/
│   ├── ChatEventPublisher.java        # STOMP message broadcasting
│   └── WebSocketEventListener.java    # Presence tracking
├── config/
│   ├── FCMInitializer.java            # Firebase Admin SDK init
│   ├── RateLimitingFilter.java        # Bucket4j filter
│   ├── RateLimitingInterceptor.java   # Bucket4j interceptor
│   ├── RedisConfig.java               # Redis / Redisson config
│   ├── SecurityConfig.java            # Spring Security config
│   ├── UserInterceptor.java           # User context resolver
│   ├── WebConfig.java                 # Web MVC config
│   └── WebsocketConfig.java           # STOMP / WebSocket config
├── controller/                        # REST controllers
├── dto/                               # Request / Response DTOs
│   ├── conversation/
│   ├── message/
│   ├── notification/
│   └── user/
├── enums/
│   ├── ConversationType.java          # PRIVATE, GROUP
│   ├── MessageType.java               # TEXT, IMG, FILE
│   └── ParticipantRole.java           # ADMIN, MEMBER
├── exception/                         # Custom exceptions
├── mapper/                            # Entity <-> DTO mappers
├── model/                             # JPA entities
├── repository/                        # Spring Data repositories
├── security/
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtHandshakeInterceptor.java
│   ├── JwtService.java
│   └── SecurityUtils.java
└── service/                           # Business logic
    └── impl/                          # Service implementations
```

---

## Running Tests

```bash
./mvnw test
```

The test suite includes unit tests for services and controllers under `src/test/java/`.

---

## Build for Production

```bash
./mvnw clean package -DskipTests
java -jar target/*.jar
```

Or build and run the Docker image:

```bash
docker build -t realtimemessage .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/realtimemessage \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=12345 \
  -e SPRING_DATA_REDIS_HOST=redis-host \
  -e JWT_SECRET=your-secret \
  realtimemessage
```

---

## License

This project is provided as a demo / educational reference.
