# Hướng dẫn test Rate Limit (Redis + Bucket4j)

Tài liệu này mô tả cách test rate limit của ứng dụng `realtime-message` trên cả **REST** và **WebSocket STOMP**, cùng với các lệnh `redis-cli` để kiểm tra / reset bucket nhanh giữa các lần test.

---

## 1. Tổng quan cơ chế

| Thành phần | File | Tác dụng |
|---|---|---|
| `RateLimitingServiceImpl` | `service/impl/RateLimitingServiceImpl.java` | Cấu hình Bucket4j + Redisson, lưu bucket trên Redis |
| `RateLimitingFilter` | `component/RateLimitingFilter.java` | Chặn HTTP request, trả 429 khi vượt limit |
| `RateLimitingInterceptor` | `component/RateLimitingInterceptor.java` | Chặn STOMP `SEND` frame, throw `MessagingException` khi vượt limit |

**Cấu hình mặc định:** 30 token / phút / user (refill greedy 30 token mỗi 1 phút, `initialTokens = 30`).

### Bucket key được tạo như thế nào?

| Loại request | Cách lấy key | Ví dụ key |
|---|---|---|
| REST có header `x-User_Id` | `"userId: " + userId` | `userId: 1` (có space sau dấu `:`) |
| REST không có header | `"IP: " + remoteAddr` | `IP: 127.0.0.1` |
| WebSocket có principal | `"ws:user:" + principal.name` | `ws:user:1` |
| WebSocket có header `x-User_Id` | `"ws:user:" + headerValue` | `ws:user:1` |
| WebSocket ẩn danh | `"ws:user:Anonyous user"` | `ws:user:Anonyous user` |

### Counter thống kê

Mỗi key sinh thêm 2 `RAtomicLong` để đếm:
- `User:<key>: Total ` &nbsp; — tổng số request (chú ý có **space cuối**)
- `User:<key>: Blocked ` — số request đã bị từ chối (cũng có **space cuối**)

> ⚠️ Tên key có chứa **dấu cách**. Khi dùng shell phải quote chuỗi và **không** dùng `xargs redis-cli del` mặc định (xargs sẽ tách theo whitespace và phá vỡ tên key).

---

## 2. Chuẩn bị môi trường

### 2.1. Khởi động stack qua Docker Compose
```bash
docker compose -f docker-compose.postgreSQL.yml up -d
```

Container Redis tên `redis-cache`, expose `6379:6379`.

### 2.2. Kiểm tra Redis hoạt động
```bash
# Nếu Redis chạy trực tiếp trên host
redis-cli PING            # → PONG

# Nếu Redis chạy trong Docker
docker exec -i redis-cache redis-cli PING   # → PONG
```

> Toàn bộ section sau giả định Redis chạy trên `localhost:6379`. Nếu chạy trong Docker, prefix mọi lệnh `redis-cli ...` bằng `docker exec -i redis-cache `.

---

## 3. Test rate limit REST

### 3.1. Bằng Postman Collection Runner
1. Import `postman_ratelimit_websocket.json`.
2. (Tuỳ chọn) chạy `Auth → Login` để cập nhật `jwtToken`.
3. Mở folder `REST - Rate Limit` → `Run` với **40 iterations**, delay 0ms.
4. Quan sát: 30 request đầu trả `200/4xx tuỳ endpoint`, từ request thứ 31 trở đi trả **HTTP 429** với header `Retry-After`.

### 3.2. Bằng curl (script bash 40 lần)
```bash
for i in $(seq 1 40); do
  echo -n "[$i] "
  curl -s -o /dev/null -w "%{http_code}\n" \
    -H "x-User_Id: 1" \
    -H "Authorization: Bearer $JWT" \
    http://localhost:8080/api/users
done
```
Output mong đợi: 30 dòng `200`, sau đó là `429`, ...

### 3.3. Log server cần thấy
```
Total access: 31
Blocked access: 1
User 1 is rate limited, wait 59 seconds
```

---

## 4. Test rate limit WebSocket STOMP

> ⚠️ Postman v2.1 **không lưu** WebSocket request vào collection. Item `WS - STOMP Connect (placeholder)` trong file collection chỉ là bookmark URL.

### 4.1. Tạo WS request thủ công trong Postman
1. `New → WebSocket Request` → URL `ws://localhost:8080/ws`.
2. (Tuỳ chọn) thêm header `Authorization: Bearer <jwt>` nếu bật `JwtHandshakeInterceptor`.
3. Click **Connect**.
4. Gửi tuần tự các frame STOMP (mỗi frame phải kết thúc bằng `\u0000` — Postman có nút insert):

**CONNECT**
```
CONNECT
accept-version:1.2
host:localhost
x-User_Id:1

^@
```

**SUBSCRIBE**
```
SUBSCRIBE
id:sub-0
destination:/topic/conversation/1

^@
```

**SEND** (gửi 40 lần, dùng feature *Resend* của Postman)
```
SEND
destination:/app/chat.send
x-User_Id:1
content-type:application/json

{"conversationId":1,"content":"hello"}
^@
```

### 4.2. Kết quả mong đợi
- Frame thứ 1–30: server xử lý bình thường.
- Frame thứ 31 trở đi: server log `User ws:user:1 is rate limited` và throw `MessagingException: Rate limiting exceeds!`. Client có thể thấy frame `ERROR` hoặc bị disconnect tuỳ STOMP client.

> Lưu ý: hiện tại `JwtHandshakeInterceptor` đang bị comment trong `WebsocketConfig`. Nếu chỉ test rate limit thì OK vì `RateLimitingInterceptor` chạy trước handler. Nếu cần test full flow (gọi `SecurityUtils.getUserId()`), bật lại interceptor và gắn JWT.

---

## 5. Kiểm tra state bucket bằng `redis-cli`

### 5.1. List các key liên quan (dry-run, không xoá)
```bash
redis-cli --scan --pattern 'ws:user:*'
redis-cli --scan --pattern 'userId:*'
redis-cli --scan --pattern 'IP:*'
redis-cli --scan --pattern 'User:*'
```

### 5.2. Xem giá trị counter
```bash
# Bucket4j lưu state nhị phân — đừng GET, dùng TYPE/STRLEN
redis-cli TYPE   'ws:user:1'
redis-cli STRLEN 'ws:user:1'

# Counter là RAtomicLong → GET trả về số
redis-cli GET 'User:ws:user:1: Total '
redis-cli GET 'User:ws:user:1: Blocked '
redis-cli GET 'User:userId: 1: Total '
redis-cli GET 'User:userId: 1: Blocked '
```

> Tên key chứa **space cuối** (sau `Total` / `Blocked`) và **space sau dấu `:`** trong `userId: 1`. Phải quote bằng single-quote.

### 5.3. Đếm số request đã chặn
```bash
redis-cli GET 'User:ws:user:1: Blocked '
```

---

## 6. Reset bucket nhanh (không phải chờ refill 60s)

Bucket trên Redis tự refill 30 token mỗi phút. Nếu muốn test lại ngay, xoá key.

### 6.1. Reset cho 1 user (Redis trên host)

```bash
# Xoá an toàn với key có space — dùng while read, KHÔNG dùng xargs mặc định
redis-cli --scan --pattern 'ws:user:1'        | while read -r k; do redis-cli del "$k"; done
redis-cli --scan --pattern 'userId: 1'        | while read -r k; do redis-cli del "$k"; done
redis-cli --scan --pattern 'User:ws:user:1:*' | while read -r k; do redis-cli del "$k"; done
redis-cli --scan --pattern 'User:userId: 1:*' | while read -r k; do redis-cli del "$k"; done
```

Trên Linux/GNU có thể dùng `xargs -d '\n'` (BSD/macOS không hỗ trợ flag `-d`):
```bash
redis-cli --scan --pattern 'User:ws:user:1:*' | xargs -r -d '\n' redis-cli del
```

### 6.2. Reset cho 1 user (Redis trong Docker)
Chạy 1 `docker exec` duy nhất cho nhanh:
```bash
docker exec -i redis-cache sh -c '
for p in "ws:user:1" "userId: 1" "User:ws:user:1:*" "User:userId: 1:*"; do
  redis-cli --scan --pattern "$p" | while read -r k; do
    redis-cli del "$k"
  done
done'
```

### 6.3. Nuke toàn bộ DB (chỉ dùng ở môi trường dev)
```bash
# Host
redis-cli FLUSHDB

# Docker
docker exec -i redis-cache redis-cli FLUSHDB
```

---

## 7. Troubleshooting

| Triệu chứng | Nguyên nhân thường gặp | Cách xử lý |
|---|---|---|
| `redis-cli del` không xoá được key | Tên key có space, `xargs` đã tách sai | Dùng `while read -r` hoặc `xargs -d '\n'` |
| Mọi REST request đều 429 ngay từ đầu | Bucket cũ còn trong Redis từ lần test trước | Xoá key như mục 6 |
| WS gửi 40 frame nhưng không bị chặn | Header `x-User_Id` chưa set + chưa có principal → key fallback `ws:user:Anonyous user`, mỗi user khác nhau? | Đảm bảo set header `x-User_Id` ở frame `SEND`, hoặc bật JWT |
| `SecurityUtils.getUserId()` NPE khi gửi `/app/chat.send` | `JwtHandshakeInterceptor` bị comment trong `WebsocketConfig` | Bật lại interceptor + gửi JWT khi handshake |
| Counter `Total`/`Blocked` không tồn tại | Chưa có request nào đi qua filter/interceptor cho key đó | Gửi request thử trước khi `GET` |
| 429 nhưng `Retry-After` ghi `... seconds seconds` | Format `Retry-After` hiện trả về RFC1123 + chuỗi `" seconds"` (xem `RateLimitingFilter`) | Đây là behaviour hiện tại của code — tham khảo nếu muốn fix |

---

## 8. Tham chiếu nhanh các pattern key

```text
ws:user:<userId>             # Bucket WebSocket
userId: <id>                 # Bucket REST có x-User_Id  (chú ý space sau ":")
IP: <remoteAddr>             # Bucket REST ẩn danh
User:<key>: Total            # Counter tổng request   (space cuối)
User:<key>: Blocked          # Counter request bị chặn (space cuối)
```
