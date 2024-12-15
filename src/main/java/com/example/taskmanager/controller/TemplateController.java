package com.example.taskmanager.controller;

import com.example.taskmanager.model.TaskTemplate;
import com.example.taskmanager.service.TaskTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/templates")
public class TemplateController {
    @Autowired
    private TaskTemplateService templateService;
    
    @GetMapping
    public String listTemplates(Model model) {
        model.addAttribute("templates", templateService.getAllTemplates());
        return "templates/list";
    }
    
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("template", new TaskTemplate());
        return "templates/create";
    }
    
    @PostMapping("/create")
    public String createTemplate(@ModelAttribute TaskTemplate template) {
        templateService.saveTemplate(template);
        return "redirect:/templates";
    }
    
    @PostMapping("/use/{id}")
    public String useTemplate(@PathVariable Long id) {
        templateService.createTaskFromTemplate(id);
        return "redirect:/tasks";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("template", templateService.getTemplate(id));
        return "templates/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateTemplate(@PathVariable Long id, @ModelAttribute TaskTemplate template) {
        template.setId(id);
        templateService.saveTemplate(template);
        return "redirect:/templates";
    }

    @PostMapping("/delete/{id}")
    public String deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return "redirect:/templates";
    }
}
