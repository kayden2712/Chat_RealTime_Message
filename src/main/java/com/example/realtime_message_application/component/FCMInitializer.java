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

    @Value("${app.firebase-configuration-file}")
    String firebaseConfigPath;

    /**
     * Khi deploy lên Render (hoặc cloud), set biến môi trường FIREBASE_CREDENTIALS_BASE64
     * bằng cách encode file serviceAccountKey.json thành base64:
     *   base64 -w 0 serviceAccountKey.json
     * Nếu biến này rỗng, sẽ fallback về đọc file JSON từ classpath (dùng local dev).
     */
    @Value("${app.firebase-credentials-base64:}")
    String firebaseCredentialsBase64;

    Logger logger = LoggerFactory.getLogger(FCMInitializer.class);

    @PostConstruct
    public void initialize() {
        try {
            InputStream credentialsStream = resolveCredentials();
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

    /**
     * Ưu tiên đọc từ biến môi trường base64 (cho Render/cloud deployment).
     * Fallback về file classpath (cho local development).
     */
    private InputStream resolveCredentials() {
        // Ưu tiên 1: FIREBASE_CREDENTIALS_BASE64 env var (Render deployment)
        if (firebaseCredentialsBase64 != null && !firebaseCredentialsBase64.isBlank()) {
            logger.info("Đọc Firebase credentials từ biến môi trường base64.");
            byte[] decodedBytes = Base64.getDecoder().decode(firebaseCredentialsBase64.trim());
            return new ByteArrayInputStream(decodedBytes);
        }

        // Ưu tiên 2: File JSON từ classpath (local development)
        try {
            ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
            if (resource.exists()) {
                logger.info("Đọc Firebase credentials từ file: {}", firebaseConfigPath);
                return resource.getInputStream();
            }
        } catch (Exception e) {
            logger.warn("Không tìm thấy file Firebase credentials: {}", firebaseConfigPath);
        }

        return null;
    }
}
