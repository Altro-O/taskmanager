package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.TaskStatistics;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskAnalyticsService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    public TaskStatistics getTaskStatistics() {
        List<Task> allTasks = taskRepository.findAll();
        
        return TaskStatistics.builder()
            .totalTasks(allTasks.size())
            .completedTasks(allTasks.stream().filter(Task::isCompleted).count())
            .overdueTasks(allTasks.stream()
                .filter(t -> !t.isCompleted() && t.getDeadline() != null && t.getDeadline().isBefore(LocalDateTime.now()))
                .count())
            .tasksByCategory(getTasksByCategory(allTasks))
            .tasksByPriority(getTasksByPriority(allTasks))
            .completionRate(calculateCompletionRate(allTasks))
            .build();
    }
    
    private Map<Category, Long> getTasksByCategory(List<Task> tasks) {
        return tasks.stream()
            .collect(Collectors.groupingBy(Task::getCategory, Collectors.counting()));
    }
    
    private Map<Priority, Long> getTasksByPriority(List<Task> tasks) {
        return tasks.stream()
            .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
    }
    
    private double calculateCompletionRate(List<Task> tasks) {
        if (tasks.isEmpty()) return 0.0;
        return tasks.stream().filter(Task::isCompleted).count() * 100.0 / tasks.size();
    }
}