package com.example.realtime_message_application.component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Component
@org.springframework.context.annotation.Profile("!test")
public class FCMInitializer {

    @Value("${app.firebase-configuration-file:serviceAccountKey.json}")
    String firebaseConfigPath;

    @Value("${app.firebase-credentials-base64:}")
    String firebaseCredentialsBase64;

    Logger logger = LoggerFactory.getLogger(FCMInitializer.class);

    @PostConstruct
    public void initialize() {
        try (InputStream credentialsStream = resolveCredentials()) {
            if (credentialsStream == null) {
                logger.warn("Firebase credentials không tìm thấy. Bỏ qua khởi tạo Firebase.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase has been initialized successfully.");
            } else {
                logger.info("Firebase is already initialized, skipping initialization.");
            }
        } catch (Exception e) {
            logger.error("Error occurred while initializing Firebase.", e);
        }
    }

    private InputStream resolveCredentials() {
        // 1. Check Base64 Environment Variable (Render / Cloud)
        if (firebaseCredentialsBase64 != null && !firebaseCredentialsBase64.isBlank()) {
            logger.info("Đọc Firebase credentials từ biến môi trường base64.");
            try {
                // Remove whitespaces and newlines that often get introduced in env vars
                String cleanBase64 = firebaseCredentialsBase64.replaceAll("\\s+", "");
                byte[] decodedBytes = Base64.getDecoder().decode(cleanBase64);
                return new ByteArrayInputStream(decodedBytes);
            } catch (IllegalArgumentException e) {
                logger.error("Biến môi trường FIREBASE_CREDENTIALS_BASE64 không đúng định dạng Base64!", e);
                return null;
            }
        }

        // 2. Fallback to Classpath File (Local Development)
        try {
            if (firebaseConfigPath == null || firebaseConfigPath.isBlank()) {
                logger.warn("app.firebase-configuration-file chưa được cấu hình.");
                return null;
            }

            ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
            if (resource.exists()) {
                logger.info("Đọc Firebase credentials từ file: {}", firebaseConfigPath);
                return resource.getInputStream();
            } else {
                logger.warn("File không tồn tại trên classpath: {}", firebaseConfigPath);
            }
        } catch (Exception e) {
            logger.warn("Không thể đọc file Firebase credentials: {}", firebaseConfigPath, e);
        }

        return null;
    }
}