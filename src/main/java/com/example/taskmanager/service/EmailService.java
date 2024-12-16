package com.example.taskmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void sendVerificationCode(String email, String code) {
        log.info("Sending verification code to: {}", email);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("asdordotacs@gmail.com");
        message.setTo(email);
        message.setSubject("Код подтверждения регистрации");
        message.setText("Ваш код подтверждения: " + code + "\n" +
                "Введите этот код для завершения регистрации.");
        
        mailSender.send(message);
    }

    public void sendNotification(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("asdordotacs@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
        mailSender.send(message);
    }
} 