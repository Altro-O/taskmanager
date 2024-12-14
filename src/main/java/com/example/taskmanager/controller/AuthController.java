package com.example.taskmanager.controller;

import com.example.taskmanager.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String name,
                             Model model) {
        try {
            userService.registerUser(email, password, name);
            return "redirect:/login?verify";
        } catch (MessagingException e) {
            model.addAttribute("error", "Ошибка при отправке email для подтверждения");
            return "register";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
    
    @GetMapping("/verify")
    public String verifyUser(@RequestParam String token, Model model) {
        if (userService.verifyUser(token)) {
            model.addAttribute("message", "Аккаунт успешно подтвержден! Теперь вы можете войти.");
            return "login";
        } else {
            model.addAttribute("error", "Неверный или устаревший токен подтверждения.");
            return "login";
        }
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
} 