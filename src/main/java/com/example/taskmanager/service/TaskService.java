package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.FirestoreTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    
    @Autowired
    private FirestoreTaskRepository taskRepository;
    
    @Autowired
    private UserService userService;
    
    public List<Task> getTasksByTelegramChat(Long chatId) {
        User user = userService.getUserByTelegramId(chatId);
        return taskRepository.findByUser(user);
    }
    
    public List<Task> getTodayTasks(Long chatId) {
        User user = userService.getUserByTelegramId(chatId);
        return taskRepository.findByUserAndDeadlineToday(user);
    }
    
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }
    
    public void completeTask(Long chatId, Long taskId) {
        User user = userService.getUserByTelegramId(chatId);
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Задача не найдена"));
            
        if (!task.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("У вас нет прав на эту задачу");
        }
        
        task.setCompleted(true);
        taskRepository.save(task);
    }
} 