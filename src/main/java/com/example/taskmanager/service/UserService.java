package com.example.taskmanager.service;

import com.example.taskmanager.model.TelegramAuthData;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.VerificationToken;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.repository.VerificationTokenRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VerificationTokenRepository tokenRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public void registerUser(String email, String password, String name) throws MessagingException {
        log.info("Attempting to register user with email: {}", email);
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("User with email {} already exists", email);
            throw new RuntimeException("Пользователь с таким email уже существует");
        }
        
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setEnabled(false);
        userRepository.save(user);
        
        String verificationCode = generateVerificationCode();
        createVerificationToken(user, verificationCode);
        emailService.sendVerificationCode(user.getEmail(), verificationCode);
    }
    
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
    
    public boolean verifyUser(String code) {
        VerificationToken verificationToken = tokenRepository.findByCode(code)
            .orElse(null);
        if (verificationToken == null) {
            return false;
        }
        
        User user = verificationToken.getUser();
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(verificationToken);
            return false;
        }
        
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
        return true;
    }
    
    private void createVerificationToken(User user, String token) {
        VerificationToken verificationToken = new VerificationToken(user, token);
        tokenRepository.save(verificationToken);
    }
    
    public void resendVerificationCode(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (user.isEnabled()) {
            throw new RuntimeException("Пользователь уже подтвержден");
        }
        
        // Удаляем старый токен, если есть
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
        
        // Сздаем новый код и отправляем
        String verificationCode = generateVerificationCode();
        createVerificationToken(user, verificationCode);
        emailService.sendVerificationCode(user.getEmail(), verificationCode);
    }
    
    public void linkTelegramChat(String email, Long chatId) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        if (!user.isEnabled()) {
            throw new RuntimeException("Аккаунт не подтвержден");
        }
        
        user.setTelegramChatId(chatId);
        userRepository.save(user);
    }
    
    public User findByTelegramChatId(Long chatId) {
        return userRepository.findByTelegramChatId(chatId)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
    
    public void addReminder(User user, int hours) {
        if (hours <= 0) {
            throw new RuntimeException("Количество часов должно быть положительным");
        }
        user.getReminderHours().add(hours);
        userRepository.save(user);
    }
    
    public void removeReminder(User user, int hours) {
        if (!user.getReminderHours().remove(hours)) {
            throw new RuntimeException("Напоминание за " + hours + " часов не найдено");
        }
        userRepository.save(user);
    }
    
    public void clearReminders(User user) {
        user.getReminderHours().clear();
        userRepository.save(user);
    }
    
    public boolean verifyTelegramAuth(TelegramAuthData data) {
        try {
            String checkString = String.format(
                "auth_date=%s\nfirst_name=%s\nid=%s\nusername=%s",
                data.getAuthDate(), data.getFirstName(), data.getId(), data.getUsername()
            );
            return verifyTelegramHash(checkString, data.getHash());
        } catch (Exception e) {
            log.error("Ошибка при проверке данных Telegram", e);
            return false;
        }
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
    
    private boolean verifyTelegramHash(String dataCheckString, String hash) {
        try {
            String botToken = "7706100075:AAHgYhlS5Lg8upPrm-Sjrd870OYSUFPhDDY";
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(botToken.getBytes(), "HmacSHA256");
            sha256HMAC.init(secretKey);
            byte[] hashBytes = sha256HMAC.doFinal(dataCheckString.getBytes());
            return hash.equals(bytesToHex(hashBytes));
        } catch (Exception e) {
            log.error("Ошибка при проверке хеша Telegram", e);
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
    
    public User findByEmail(String email) {
        log.info("Looking for user with email: {}", email);
        return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("User not found with email: {}", email);
                return new RuntimeException("Пользователь не найден");
            });
    }
} 