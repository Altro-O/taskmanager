package com.example.taskmanager.controller;

import com.example.taskmanager.model.TelegramAuthData;
import com.example.taskmanager.model.User;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
            redirectAttributes.addFlashAttribute("successMessage", "Регистрация успешна! ��еперь вы можете войти.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/telegram/auth")
    @ResponseBody
    public ResponseEntity<String> handleTelegramAuth(@RequestBody TelegramAuthData authData) {
        try {
            if (userService.verifyTelegramAuth(authData)) {
                Long telegramId = authData.getId();
                User user = userService.getUserByTelegramId(telegramId);
                if (user != null) {
                    return ResponseEntity.ok("success");
                }
            }
            return ResponseEntity.badRequest().body("error: Authentication failed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("error: " + e.getMessage());
        }
    }

    @GetMapping("/telegram/login")
    public String showTelegramLogin() {
        return "telegram_login";
    }
}