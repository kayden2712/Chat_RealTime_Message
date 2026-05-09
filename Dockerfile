# Bước 1: Sử dụng JRE gọn nhẹ để chạy ứng dụng (Alpine giúp giảm kích thước image)
FROM eclipse-temurin:21-jre-alpine

# Bước 2: Thiết lập thư mục làm việc trong container
WORKDIR /app

# Bước 3: Copy file jar đã build từ máy host vào container
# Lưu ý: Bạn cần chạy lệnh 'mvn clean package' trước khi build docker
COPY target/*.jar app.jar

# Bước 4: Mở cổng 8080 cho ứng dụng
EXPOSE 8080

# Bước 5: Lệnh thực thi ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]