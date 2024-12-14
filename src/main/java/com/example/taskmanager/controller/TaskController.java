package com.example.taskmanager.controller;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.service.FirebaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/task/create")
    public String createTask(@ModelAttribute Task task) {
        Task savedTask = taskRepository.save(task);
        firebaseService.syncTask(savedTask); // Синхронизация с Firebase
        return "redirect:/";
    }

    @PostMapping("/task/complete/{id}")
    public String toggleTask(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setCompleted(!task.isCompleted());
        taskRepository.save(task);
        firebaseService.syncTask(task); // Синхронизация с Firebase
        return "redirect:/";
    }

    @PostMapping("/task/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskRepository.deleteById(id);
        firebaseService.deleteTask(id); // Удаление из Firebase
        return "redirect:/";
    }

    @GetMapping("/task/edit/{id}")
    public String editTask(@PathVariable Long id, Model model) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        model.addAttribute("task", task);
        return "edit";
    }

    @PostMapping("/task/update/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute Task task) {
        task.setId(id);
        Task updatedTask = taskRepository.save(task);
        firebaseService.syncTask(updatedTask); // Синхронизация с Firebase
        return "redirect:/";
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Task> tasks = taskRepository.findAll();
        model.addAttribute("tasks", tasks != null ? tasks : new ArrayList<>());
        return "index";
    }
}

