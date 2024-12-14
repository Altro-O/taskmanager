package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    
    @Autowired
    private TaskRepository taskRepository;

    // Проверка дедлайнов каждый час
    @Scheduled(fixedRate = 3600000)
    public void checkDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayAhead = now.plusDays(1);

        List<Task> tasksToNotify = taskRepository.findAll().stream()
            .filter(task -> !task.isCompleted())
            .filter(task -> !task.isNotified())
            .filter(task -> task.getDeadline() != null)
            .filter(task -> task.getDeadline().isBefore(dayAhead))
            .toList();

        for (Task task : tasksToNotify) {
            sendNotification(task);
            task.setNotified(true);
            taskRepository.save(task);
        }
    }

    private void sendNotification(Task task) {
        // В реальном приложении здесь был бы код отправки уведомления
        System.out.println("УВЕДОМЛЕНИЕ: Задача '" + task.getTitle() + 
                         "' должна быть выполнена до " + task.getDeadline());
    }
} 