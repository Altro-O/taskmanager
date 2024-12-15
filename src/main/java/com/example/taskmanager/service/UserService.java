package com.example.taskmanager.service;

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

@Service
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
        if (userRepository.findByEmail(email).isPresent()) {
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
        
        // Создаем новый код и отправляем
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
} 