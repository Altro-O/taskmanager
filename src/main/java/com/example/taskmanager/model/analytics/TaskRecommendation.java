package com.example.taskmanager.model.analytics;

import lombok.Data;
import lombok.Builder;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.TimeLabel;

@Data
@Builder
public class TaskRecommendation {
    private RecommendationType type;
    private String message;
    private Priority suggestedPriority;
    private TimeLabel suggestedTime;
    private Double expectedDuration;
    
    public enum RecommendationType {
        TIME_MANAGEMENT,
        PRIORITY_ADJUSTMENT,
        WORKLOAD_BALANCE,
        DEADLINE_RISK
    }
} 