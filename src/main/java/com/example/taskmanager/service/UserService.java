package com.example.taskmanager.service;

import com.example.taskmanager.model.TelegramAuthData;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public void registerUser(String email, String password, String name) {
        log.info("Attempting to register user with email: {}", email);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setEnabled(true);
        
        userRepository.save(user);
        log.info("User registered successfully: {}", email);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public User getUserByTelegramId(Long telegramId) {
        return userRepository.findByTelegramChatId(telegramId)
            .orElseGet(() -> {
                User user = new User();
                user.setTelegramChatId(telegramId);
                user.setEnabled(true);
                return userRepository.save(user);
            });
    }

    public boolean verifyTelegramAuth(TelegramAuthData data) {
        try {
            StringBuilder checkString = new StringBuilder();
            if (data.getAuthDate() != null) checkString.append("auth_date=").append(data.getAuthDate()).append("\n");
            if (data.getFirstName() != null) checkString.append("first_name=").append(data.getFirstName()).append("\n");
            if (data.getId() != null) checkString.append("id=").append(data.getId()).append("\n");
            if (data.getUsername() != null) checkString.append("username=").append(data.getUsername());
            
            // Удаляем последний перенос строки, если он есть
            String finalCheckString = checkString.toString();
            if (finalCheckString.endsWith("\n")) {
                finalCheckString = finalCheckString.substring(0, finalCheckString.length() - 1);
            }
            
            return verifyTelegramHash(finalCheckString, data.getHash());
        } catch (Exception e) {
            log.error("Ошибка при проверке данных Telegram", e);
            return false;
        }
    }

    private boolean verifyTelegramHash(String dataCheckString, String hash) {
        try {
            String botToken = "7706100075:AAHgYhlS5Lg8upPrm-Sjrd870OYSUFPhDDY";
            // Используем только секретный ключ из токена
            String secretKey = botToken.split(":")[1];
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256HMAC.init(secretKeySpec);
            byte[] hashBytes = sha256HMAC.doFinal(dataCheckString.getBytes());
            return hash.equals(bytesToHex(hashBytes));
        } catch (Exception e) {
            log.error("Ошибка при проверке хеша Telegram: {}", e.getMessage());
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}