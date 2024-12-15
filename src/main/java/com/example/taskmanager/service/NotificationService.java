package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
public class NotificationService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TelegramService telegramService;
    
    @Autowired
    private EmailService emailService;

    @Scheduled(fixedRate = 900000) // Каждые 15 минут
    public void checkDeadlinesAndNotify() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> tasks = taskRepository.findByCompletedFalse();
        
        for (Task task : tasks) {
            User user = task.getUser();
            if (task.getDeadline() == null || task.isNotified()) continue;
            
            for (Integer hours : user.getReminderHours()) {
                LocalDateTime reminderTime = task.getDeadline().minusHours(hours);
                if (now.isAfter(reminderTime) && now.isBefore(reminderTime.plusMinutes(15))) {
                    sendNotification(task, hours);
                    if (hours == Collections.min(user.getReminderHours())) {
                        task.setNotified(true);
                        taskRepository.save(task);
                    }
                }
            }
        }
    }

    private void sendNotification(Task task, int hours) {
        User user = task.getUser();
        String message = String.format(
            "⚠️ Напоминание!\nЗадача \"%s\" должна быть выполнена через %d %s\n" +
            "Дедлайн: %s\nПриоритет: %s",
            task.getTitle(),
            hours,
            formatHours(hours),
            formatDateTime(task.getDeadline()),
            task.getPriority().getDisplayName()
        );

        if (user.isEnableTelegramNotifications() && user.getTelegramChatId() != null) {
            telegramService.sendNotification(user.getTelegramChatId(), message);
        }

        if (user.isEnableEmailNotifications()) {
            emailService.sendNotification(user.getEmail(), "Напоминание о за��аче", message);
        }
    }

    private String formatHours(int hours) {
        if (hours % 10 == 1 && hours != 11) return "час";
        if (hours % 10 >= 2 && hours % 10 <= 4 && (hours < 12 || hours > 14)) return "часа";
        return "часов";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
} 