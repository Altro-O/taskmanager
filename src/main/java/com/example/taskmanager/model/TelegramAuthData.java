package com.example.taskmanager.model;

import lombok.Data;

@Data
public class TelegramAuthData {
    private Long id;
    private String firstName;
    private String username;
    private Long authDate;
    private String hash;
} 