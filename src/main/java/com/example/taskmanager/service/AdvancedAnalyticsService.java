package com.example.taskmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.TimeLabel;
import com.example.taskmanager.model.analytics.AnalyticsTrend;
import com.example.taskmanager.repository.TaskRepository;

@Service
@Slf4j
public class AdvancedAnalyticsService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    public Map<Category, Double> getAverageCompletionTimeByCategory() {
        List<Task> completedTasks = taskRepository.findByCompleted(true);
        return completedTasks.stream()
            .filter(task -> task.getCompletedAt() != null)
            .collect(Collectors.groupingBy(
                Task::getCategory,
                Collectors.averagingLong(task -> 
                    ChronoUnit.MINUTES.between(task.getCreatedAt(), task.getCompletedAt())
                )
            ));
    }
    
    public Map<LocalDate, Integer> getWorkloadForecast() {
        LocalDate today = LocalDate.now();
        LocalDate twoWeeksAhead = today.plusWeeks(2);
        
        List<Task> tasks = taskRepository.findByDeadlineBetween(
            today.atStartOfDay(),
            twoWeeksAhead.atTime(23, 59)
        );
        
        Map<LocalDate, Integer> workload = new TreeMap<>();
        for (LocalDate date = today; date.isBefore(twoWeeksAhead); date = date.plusDays(1)) {
            workload.put(date, 0);
        }
        
        tasks.forEach(task -> {
            LocalDate deadline = task.getDeadline().toLocalDate();
            workload.merge(deadline, 1, Integer::sum);
        });
        
        return workload;
    }
    
    public Map<TimeLabel, Double> getProductivityByTimeOfDay() {
        List<Task> completedTasks = taskRepository.findByCompleted(true);
        
        return completedTasks.stream()
            .filter(task -> task.getCompletedAt() != null)
            .collect(Collectors.groupingBy(
                task -> TimeLabel.fromDateTime(task.getCompletedAt()),
                Collectors.averagingLong(task -> 
                    task.getEstimatedMinutes() > 0 ? 
                    task.getEstimatedMinutes() / ChronoUnit.MINUTES.between(
                        task.getCreatedAt(), task.getCompletedAt()
                    ) : 0
                )
            ));
    }
    
    public List<AnalyticsTrend> identifyTaskPatterns() {
        List<AnalyticsTrend> trends = new ArrayList<>();
        
        // Анализ пиков продуктивности
        Map<TimeLabel, Double> productivity = getProductivityByTimeOfDay();
        TimeLabel mostProductiveTime = productivity.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
            
        if (mostProductiveTime != null) {
            trends.add(AnalyticsTrend.builder()
                .type(AnalyticsTrend.TrendType.PRODUCTIVITY_PEAK)
                .description("Наивысшая продуктивность: " + mostProductiveTime.getName())
                .confidence(0.85)
                .data(Map.of("timeLabel", mostProductiveTime))
                .build());
        }
        
        // Другие паттерны...
        
        return trends;
    }
} 