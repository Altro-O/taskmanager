package com.example.taskmanager.model.analytics;

import lombok.Data;
import lombok.Builder;
import java.util.Map;

@Data
@Builder
public class AnalyticsTrend {
    private TrendType type;
    private String description;
    private double confidence; // 0-1
    private Map<String, Object> data;
    
    public enum TrendType {
        PRODUCTIVITY_PEAK,
        TASK_PATTERN,
        WORKLOAD_TREND,
        COMPLETION_RATE
    }
} 