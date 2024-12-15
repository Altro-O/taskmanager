package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskTemplate;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.TaskTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskTemplateService {
    @Autowired
    private TaskTemplateRepository templateRepository;
    
    @Autowired
    private TaskRepository taskRepository;

    public List<TaskTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    public TaskTemplate saveTemplate(TaskTemplate template) {
        return templateRepository.save(template);
    }

    public TaskTemplate getTemplate(Long id) {
        return templateRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Template not found"));
    }

    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    public void createTaskFromTemplate(Long templateId) {
        TaskTemplate template = getTemplate(templateId);
        Task task = new Task();
        task.setTitle(template.getName());
        task.setDescription(template.getDescription());
        task.setCategory(template.getCategory());
        task.setPriority(template.getPriority());
        task.setTags(template.getDefaultTags());
        taskRepository.save(task);
    }
}
