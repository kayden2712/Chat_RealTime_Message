# Sử dụng JDK 17 (hoặc phiên bản bạn đang dùng)
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy file jar đã build (đảm bảo tên file khớp với thư mục target của bạn)
COPY target/*.jar app.jar

# Copy file cấu hình Firebase vào đúng thư mục bên trong container
#COPY src/main/resources/firebase/ /app/firebase/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]