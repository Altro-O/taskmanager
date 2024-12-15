package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserService userService;
    
    public List<Task> getTasksByTelegramChat(Long chatId) {
        User user = userService.findByTelegramChatId(chatId);
        return taskRepository.findByUser(user);
    }
    
    public List<Task> getTodayTasks(Long chatId, LocalDateTime start, LocalDateTime end) {
        User user = userService.findByTelegramChatId(chatId);
        return taskRepository.findByUserAndDeadlineBetween(user, start, end);
    }
    
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }
    
    public void completeTask(Long chatId, Long taskId) {
        User user = userService.findByTelegramChatId(chatId);
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Задача не найдена"));
            
        if (!task.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("У вас нет доступа к этой задаче");
        }
        
        task.setCompleted(true);
        taskRepository.save(task);
    }
} 