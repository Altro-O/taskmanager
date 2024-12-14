package com.example.taskmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("asdordotacs@gmail.com");  // Должен совпадать с username в properties
        message.setTo(to);
        message.setSubject("Подтверждение регистрации");
        message.setText("Для подтверждения регистрации перейдите по ссылке: \n" +
                "http://localhost:8080/confirm?token=" + token);
        
        mailSender.send(message);
    }
} 