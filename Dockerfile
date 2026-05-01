# Bước 1: Sử dụng JRE gọn nhẹ để chạy ứng dụng (Alpine giúp giảm kích thước image)
FROM eclipse-temurin:21-jre-alpine

# Bước 2: Thiết lập thư mục làm việc trong container
WORKDIR /app

# Bước 3: Copy file jar đã build từ máy host vào container
# Lưu ý: Bạn cần chạy lệnh 'mvn clean package' trước khi build docker
COPY target/*.jar app.jar

# Bước 4: Xử lý file cấu hình Firebase
# Trong ảnh, file nằm ở src/main/resources/serviceAccountKey.json
# Khi build jar, Spring sẽ đưa nó vào thư mục gốc của classpath.
# Do đó, chúng ta thường không cần copy thủ công thư mục src vào đây
# vì nó đã nằm bên trong file app.jar rồi.

# Bước 5: Mở cổng 8080 cho ứng dụng
EXPOSE 8080

# Bước 6: Lệnh thực thi ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]