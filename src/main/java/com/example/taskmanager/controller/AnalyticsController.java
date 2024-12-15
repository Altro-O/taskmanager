package com.example.taskmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.taskmanager.model.User;
import com.example.taskmanager.service.AdvancedAnalyticsService;
import com.example.taskmanager.service.RecommendationService;
import com.example.taskmanager.model.analytics.AnalyticsTrend;
import com.example.taskmanager.model.analytics.TaskRecommendation;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {
    
    @Autowired
    private AdvancedAnalyticsService analyticsService;
    
    @Autowired
    private RecommendationService recommendationService;
    
    @GetMapping("/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("completionTimes", analyticsService.getAverageCompletionTimeByCategory());
        model.addAttribute("workload", analyticsService.getWorkloadForecast());
        model.addAttribute("productivity", analyticsService.getProductivityByTimeOfDay());
        model.addAttribute("trends", analyticsService.identifyTaskPatterns());
        model.addAttribute("recommendations", recommendationService.getRecommendations(user));
        
        return "analytics/dashboard";
    }
    
    @GetMapping("/charts/data")
    @ResponseBody
    public Map<String, Object> getChartData() {
        Map<String, Object> data = new HashMap<>();
        data.put("completionTimes", analyticsService.getAverageCompletionTimeByCategory());
        data.put("workload", analyticsService.getWorkloadForecast());
        data.put("productivity", analyticsService.getProductivityByTimeOfDay());
        return data;
    }
} 