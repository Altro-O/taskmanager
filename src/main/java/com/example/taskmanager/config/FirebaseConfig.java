package com.example.taskmanager.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class FirebaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${FIREBASE_CONFIG:}")
    private String firebaseConfigEnv;

    @PostConstruct
    public void initialize() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                logger.info("Firebase уже инициализирован");
                return;
            }

            InputStream serviceAccount = null;
            
            // Сначала пробуем использовать переменную окружения
            if (firebaseConfigEnv != null && !firebaseConfigEnv.trim().isEmpty()) {
                try {
                    byte[] decodedConfig = Base64.getDecoder().decode(firebaseConfigEnv);
                    serviceAccount = new ByteArrayInputStream(decodedConfig);
                    logger.info("Используется конфигурация Firebase из переменной окружения");
                } catch (IllegalArgumentException e) {
                    logger.warn("Ошибка декодирования конфигурации Firebase из переменной окружения: {}", e.getMessage());
                }
            }

            // Если не получилось использовать переменную окружения, пробуем локальный файл
            if (serviceAccount == null) {
                serviceAccount = getClass().getResourceAsStream("/firebase-config.json");
                if (serviceAccount != null) {
                    logger.info("Используется локальный файл конфигурации Firebase");
                }
            }

            // Если оба способа не сработали, логируем ошибку и выходим
            if (serviceAccount == null) {
                logger.error("Не удалось найти конфигурацию Firebase. Убедитесь, что файл firebase-config.json находится в resources или установлена переменная окружения FIREBASE_CONFIG");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            logger.info("Firebase успешно инициализирован");

        } catch (IOException e) {
            logger.error("Ошибка при инициализации Firebase: {}", e.getMessage());
            logger.info("Приложение продолжит работу без интеграции с Firebase");
        }
    }
}