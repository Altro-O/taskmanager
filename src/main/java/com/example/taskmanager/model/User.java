package com.example.taskmanager.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String email;
    private String password;
    private boolean enabled;
    private String name;
    private Long telegramChatId;

    @ElementCollection
    private Set<Integer> reminderHours = new HashSet<>(Arrays.asList(24, 1)); // По умолчанию за 24 часа и за 1 час

    private boolean enableTelegramNotifications = true;
    private boolean enableEmailNotifications = true;

    // Геттеры
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public Long getTelegramChatId() {
        return telegramChatId;
    }

    public Set<Integer> getReminderHours() {
        return reminderHours;
    }

    public boolean isEnableTelegramNotifications() {
        return enableTelegramNotifications;
    }

    public boolean isEnableEmailNotifications() {
        return enableEmailNotifications;
    }

    // Сеттеры
    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTelegramChatId(Long telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public void setReminderHours(Set<Integer> reminderHours) {
        this.reminderHours = reminderHours;
    }

    public void setEnableTelegramNotifications(boolean enableTelegramNotifications) {
        this.enableTelegramNotifications = enableTelegramNotifications;
    }

    public void setEnableEmailNotifications(boolean enableEmailNotifications) {
        this.enableEmailNotifications = enableEmailNotifications;
    }
}
