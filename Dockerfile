# ==========================================
# STAGE 1: Build file JAR từ mã nguồn
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml và tải trước các dependency (giúp cache build nhanh hơn)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn và build dự án
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Chạy ứng dụng bằng JRE Alpine gọn nhẹ
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy file jar được tạo ra từ STAGE 1 sang STAGE 2
COPY --from=builder /app/target/*.jar app.jar

# Giới hạn RAM JVM cho Render Free (512MB RAM) tránh OOM
# Render inject PORT env tự động, Spring Boot đọc qua server.port=${PORT:8080}
ENV JAVA_OPTS="-Xms128m -Xmx350m -XX:+UseContainerSupport -XX:MaxRAMPercentage=70.0"

# Render sẽ override PORT, không hardcode
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]