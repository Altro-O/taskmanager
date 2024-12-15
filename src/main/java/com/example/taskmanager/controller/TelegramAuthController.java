package com.example.taskmanager.controller;

import com.example.taskmanager.model.User;
import com.example.taskmanager.model.TelegramAuthData;
import com.example.taskmanager.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Controller
@RequestMapping("/telegram-auth")
@Slf4j
public class TelegramAuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String telegramLogin(Model model) {
        String botUsername = "TaskManag3rBot";
        model.addAttribute("botUsername", botUsername);
        return "telegram-login";
    }
    
    @PostMapping("/callback")
    @ResponseBody
    public ResponseEntity<?> handleTelegramAuth(@RequestBody TelegramAuthData data) {
        try {
            if (userService.verifyTelegramAuth(data)) {
                User user = userService.getUserByTelegramId(data.getId());
                
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    user, null, Collections.emptyList()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Ошибка при авторизации через Telegram", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 