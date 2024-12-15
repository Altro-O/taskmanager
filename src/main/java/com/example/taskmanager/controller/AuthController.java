package com.example.taskmanager.controller;

import com.example.taskmanager.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
                             RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(email, password, name);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/verify";
        } catch (MessagingException e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при отправке кода подтверждения");
            return "redirect:/register";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
    
    @GetMapping("/verify")
    public String showVerificationPage(@ModelAttribute("email") String email, Model model) {
        if (email == null || email.isEmpty()) {
            return "redirect:/register";
        }
        model.addAttribute("email", email);
        return "verify";
    }
    
    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String code, 
                           RedirectAttributes redirectAttributes) {
        if (userService.verifyUser(code)) {
            redirectAttributes.addFlashAttribute("message", 
                "Email успешно подтвержден! Теперь вы можете войти.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", 
                "Неверный или устаревший код подтверждения.");
            return "redirect:/verify";
        }
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/resend-code")
    public String resendVerificationCode(@RequestParam String email, 
                                       RedirectAttributes redirectAttributes) {
        try {
            userService.resendVerificationCode(email);
            redirectAttributes.addFlashAttribute("message", 
                "Новый код подтверждения отправлен на вашу почту");
        } catch (MessagingException e) {
            redirectAttributes.addFlashAttribute("error", 
                "Ошибка при отправке нового кода подтверждения");
        }
        return "redirect:/verify";
    }
} 