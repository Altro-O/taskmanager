package com.example.taskmanager.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class TaskStatistics {
    private long totalTasks;
    private long completedTasks;
    private long overdueTasks;
    private Map<Category, Long> tasksByCategory;
    private Map<Priority, Long> tasksByPriority;
    private double completionRate;
}
