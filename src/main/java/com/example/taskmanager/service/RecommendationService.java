package com.example.taskmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.TimeLabel;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.analytics.TaskRecommendation;
import com.example.taskmanager.repository.TaskRepository;

@Service
@Slf4j
public class RecommendationService {
    
    @Autowired
    private AdvancedAnalyticsService analyticsService;
    
    @Autowired
    private TaskRepository taskRepository;
    
    public List<TaskRecommendation> getRecommendations(User user) {
        List<TaskRecommendation> recommendations = new ArrayList<>();
        
        // Анализ загруженности
        Map<LocalDate, Integer> workload = analyticsService.getWorkloadForecast();
        OptionalDouble avgWorkload = workload.values().stream()
            .mapToInt(Integer::intValue)
            .average();
            
        if (avgWorkload.isPresent()) {
            workload.forEach((date, tasks) -> {
                if (tasks > avgWorkload.getAsDouble() * 1.5) {
                    recommendations.add(TaskRecommendation.builder()
                        .type(TaskRecommendation.RecommendationType.WORKLOAD_BALANCE)
                        .message("Высокая нагрузка " + date + ": " + tasks + " задач")
                        .build());
                }
            });
        }
        
        // Рекомендации по времени
        Map<TimeLabel, Double> productivity = analyticsService.getProductivityByTimeOfDay();
        TimeLabel bestTime = productivity.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
            
        if (bestTime != null) {
            recommendations.add(TaskRecommendation.builder()
                .type(TaskRecommendation.RecommendationType.TIME_MANAGEMENT)
                .message("Планируйте сложные задачи на " + bestTime.getName())
                .suggestedTime(bestTime)
                .build());
        }
        
        return recommendations;
    }
    
    public TimeLabel suggestBestTimeForTask(Task task) {
        Map<TimeLabel, Double> productivity = analyticsService.getProductivityByTimeOfDay();
        
        // Учитываем приоритет задачи
        if (task.getPriority() == Priority.HIGH) {
            return productivity.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(TimeLabel.MORNING);
        }
        
        return TimeLabel.AFTERNOON;
    }
    
    public Priority predictTaskDifficulty(Task newTask) {
        // Анализ похожих задач
        List<Task> similarTasks = taskRepository.findByCategory(newTask.getCategory());
        
        OptionalDouble avgDuration = similarTasks.stream()
            .filter(task -> task.getCompletedAt() != null)
            .mapToLong(task -> ChronoUnit.MINUTES.between(
                task.getCreatedAt(), task.getCompletedAt()
            ))
            .average();
            
        if (avgDuration.isPresent()) {
            if (avgDuration.getAsDouble() > 240) return Priority.HIGH;
            if (avgDuration.getAsDouble() > 60) return Priority.MEDIUM;
        }
        
        return Priority.LOW;
    }
} 