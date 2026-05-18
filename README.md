# RealTime Message Application

A real-time messaging backend built with **Spring Boot 3.5**, **WebSocket (STOMP)**, **PostgreSQL**, **Redis**, and **Firebase Cloud Messaging (FCM)**. Supports private and group conversations with features like message pinning, read receipts, user blocking, muting, and rate limiting.

---

## Tech Stack

| Category          | Technology                                                    |
| ----------------- | ------------------------------------------------------------- |
| Framework         | Spring Boot 3.5.11, Spring Security, Spring Data JPA          |
| Language          | Java 21                                                       |
| Real-time         | WebSocket (STOMP over SockJS)                                 |
| Database          | PostgreSQL 18                                                 |
| Cache / Pub-Sub   | Redis (Redisson 3.52.0)                                       |
| Push Notification | Firebase Cloud Messaging (firebase-admin 9.7.0)               |
| Rate Limiting     | Bucket4j 8.14.0 (distributed via Redisson)                    |
| Authentication    | JWT (jjwt 0.13.0), OAuth2 Resource Server                    |
| Build             | Maven Wrapper (`./mvnw`)                                      |
| Containerization  | Docker, Docker Compose                                        |

---

## Features

### Messaging
- **Real-time chat** over WebSocket (STOMP) for private and group conversations
- **Message types:** TEXT, IMG, FILE
- **Edit & delete** messages with soft-delete and restore
- **Pin / unpin** messages within a conversation
- **Read receipts** per message per user

### Conversations
- **Private** (1-on-1) and **Group** conversations
- **Participant management:** add, remove, leave
- **Roles:** ADMIN, MEMBER
- **Mute / unmute** conversations
- **Archive** conversations
- **Mark as favorite**
- **Update** title, description, and image

### User Management
- User profile with bio and profile picture
- **Block / unblock** other users
- **Ban / unban** users from conversations (group moderation)
- **Online presence** tracking via WebSocket connect/disconnect events

### Security & Infrastructure
- **JWT-based authentication** with access and refresh tokens
- **OAuth2 Resource Server** support
- **Rate limiting** per user/IP via Bucket4j backed by Redis
- **Push notifications** via Firebase Cloud Messaging (FCM)
- **Scheduled tasks** for cleanup and maintenance

---

## Architecture Overview

```mermaid
graph TD
    A[Client - Web / Mobile] -->|REST API| B[Spring Boot Application]
    A -->|WebSocket STOMP| C[WebSocket Endpoints]
    B --> D[PostgreSQL]
    B --> E[Redis - Cache / Rate Limiting]
    ---

    ## English README — Full (with examples)

    Project: realtimemessage

    Short description
    A real-time messaging backend built with Spring Boot, WebSocket (STOMP), PostgreSQL, Redis and Firebase Cloud Messaging (FCM). It supports private and group conversations, message pinning, read receipts, blocking/ban, rate limiting (Bucket4j + Redis/Redisson), and push notifications.

    Table of contents
    - Overview
    - Tech stack
    - Prerequisites
    - Environment variables
    - Run with Docker Compose
    - Run locally (development)
    - Configuration notes
    - WebSocket / STOMP examples
    - REST API examples (curl)
    - Tests
    - Build & production
    - Troubleshooting
    - Contributing

    Overview
    This backend exposes REST APIs and WebSocket (STOMP) endpoints. Use REST for management (create conversation, upload files, register FCM tokens) and WebSocket/STOMP for real-time message exchange and presence.

    Tech stack
    - Spring Boot 3.5
    - Java 21
    - WebSocket (STOMP over SockJS)
    - PostgreSQL 18
    - Redis (cache / pub-sub)
    - Redisson + Bucket4j (rate limiting)
    - Firebase Admin SDK (FCM)
    - Maven (`./mvnw`)

    Prerequisites
    - Docker & Docker Compose
    - Java 21 (only needed for running locally)
    - Maven (use `./mvnw` included)
    - Firebase service account JSON (for FCM features)

    Environment variables (`.env` example)
    Create a `.env` in project root used by `docker-compose.postgreSQL.yml`:

    POSTGRES_USER=postgres
    POSTGRES_PASSWORD=postgrespass
    POSTGRES_DB_NAME=realtimedb
    REDIS_PORT=6379
    JWT_SECRET_KEY=your_jwt_secret

    Note: Spring reads `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATA_REDIS_HOST`/`SPRING_DATA_REDIS_PORT`, `JWT_SECRET` (or `JWT_SECRET_KEY`) from env or `application.properties`.

    Run with Docker Compose
    Start services (Postgres, Redis, app):

    ```bash
    docker compose -f docker-compose.postgreSQL.yml --env-file .env up --build -d
    ```

    Check status:

    ```bash
    docker ps
    docker logs -f realtimemessage-service
    ```

    Stop and remove:

    ```bash
    docker compose -f docker-compose.postgreSQL.yml down
    ```

    Run locally (development)
    1. Ensure PostgreSQL and Redis are available (local or via Docker).
    2. Configure `src/main/resources/application.properties` or export environment variables.
    3. Run:

    ```bash
    ./mvnw spring-boot:run
    ```

    Application default: http://localhost:8080

    Configuration notes
    - `SPRING_DATASOURCE_URL` example: `jdbc:postgresql://db-postgres:5432/realtimedb` (use service name `db-postgres` when using compose)
    - `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` — from `.env`
    - `SPRING_DATA_REDIS_HOST` / `SPRING_DATA_REDIS_PORT` — `redis-cache` when using compose
    - `JWT_SECRET` or `JWT_SECRET_KEY` must be set for JWT signing
    - `serviceAccountKey.json` for Firebase should be placed into `src/main/resources` or path set via `app.firebase-configuration-file`

    WebSocket / STOMP (examples)
    Endpoint: `/ws` (SockJS fallback enabled)

    Typical flow (STOMP):
    1) CONNECT / STOMP handshake: include JWT in headers
    2) SUBSCRIBE to conversation topics
    3) SEND messages to application destination

    STOMP connect (client example headers):

    Headers:
    - `Authorization: Bearer <JWT>`
    - `accept-version:1.2`

    Subscribe (example):

    SUBSCRIBE to `/topic/conversations.{convId}` to receive group messages.
    SUBSCRIBE to `/user/queue/conversation.{convId}` to receive private queue messages.

    Send message (client -> server) to app destination `/app/chat.sendMessage` with JSON payload:

    {
      "conversationId": "<convId>",
      "type": "TEXT",
      "content": "Hello, world!",
      "senderId": "<userId>",
      "metadata": {}
    }

    Server broadcasts to `/topic/conversations.{convId}` or `/user/queue/conversation.{convId}` as appropriate.

    REST API examples (curl)
    Note: adapt host/port, credentials and endpoints as in your app.

    1) Authenticate (example) — assume endpoint `/api/auth/login` returns `{ accessToken, refreshToken }`:

    ```bash
    curl -X POST http://localhost:8080/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{"username":"alice","password":"password"}'
    ```

    2) Create private conversation:

    ```bash
    curl -X POST http://localhost:8080/api/conversations \
      -H "Authorization: Bearer <ACCESS_TOKEN>" \
      -H "Content-Type: application/json" \
      -d '{"type":"PRIVATE","participantIds":["user1","user2"]}'
    ```

    3) Send message via REST (if supported):

    ```bash
    curl -X POST http://localhost:8080/api/conversations/<convId>/messages \
      -H "Authorization: Bearer <ACCESS_TOKEN>" \
      -H "Content-Type: application/json" \
      -d '{"type":"TEXT","content":"Hello from REST"}'
    ```

    4) Register FCM token:

    ```bash
    curl -X POST http://localhost:8080/api/fcm/register \
      -H "Authorization: Bearer <ACCESS_TOKEN>" \
      -H "Content-Type: application/json" \
      -d '{"token":"<device_fcm_token>"}'
    ```

    WebSocket quick example (w/ STOMP JS minimal flow)
    - Connect and send JWT in connect headers
    - Subscribe to `/topic/conversations.123`
    - Send message JSON to `/app/chat.sendMessage`

    Tests
    Run unit and integration tests with:

    ```bash
    ./mvnw test
    ```

    Build & production
    Build jar:

    ```bash
    ./mvnw clean package -DskipTests
    java -jar target/*.jar
    ```

    Build docker image and run:

    ```bash
    docker build -t realtimemessage .
    docker run -p 8080:8080 \
      -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/realtimedb \
      -e SPRING_DATASOURCE_USERNAME=postgres \
      -e SPRING_DATASOURCE_PASSWORD=postgrespass \
      -e SPRING_DATA_REDIS_HOST=redis-host \
      -e JWT_SECRET=your-secret \
      realtimemessage
    ```

    Troubleshooting
    - If app cannot reach DB: check `SPRING_DATASOURCE_URL`, container/service `db-postgres` and `docker logs -f postgres-db`.
    - If Redis/rate-limiting errors: check `redis-cache` container and `RedisConfig.java`.
    - If FCM errors: validate `serviceAccountKey.json` and Firebase project settings.

    Contributing
    - Fork, create a feature branch, add tests where appropriate, open a PR with description and test results.

    Extras
    - I can generate a Postman collection or sample Postman export for the main REST endpoints and a minimal STOMP client snippet — tell me if you want that and I will add it.

| Destination                              | Type    | Description                              |
| ---------------------------------------- | ------- | ---------------------------------------- |
| `/topic/conversations.{convId}`          | Topic   | Group conversation messages              |
| `/user/queue/conversation.{convId}`      | Queue   | Private conversation messages per user   |
| `/app/chat.sendMessage`                  | App     | Send a new message                       |
| `/app/chat.editMessage`                  | App     | Edit an existing message                 |
| `/app/chat.deleteMessage`                | App     | Soft-delete a message                    |

---

## Database Models

| Entity                      | Table                  | Description                               |
| --------------------------- | ---------------------- | ----------------------------------------- |
| `User`                      | `users`                | Application user with profile info        |
| `Conversation`              | `conversations`        | Chat conversation (PRIVATE / GROUP)       |
| `ConversationParticipant`   | `conversation_participants` | Many-to-many join with role          |
| `Message`                   | `messages`             | Chat message with type and soft-delete    |
| `ReadReceipt`               | `read_receipts`        | Per-user read status per message          |
| `Block`                     | `blocks`               | User blocking relationships               |
| `BannedUser`                | `banned_users`         | Banned users in conversations             |
| `FCMToken`                  | `fcm_tokens`           | Firebase device tokens per user           |

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
