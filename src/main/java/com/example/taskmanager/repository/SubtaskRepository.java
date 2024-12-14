package com.example.taskmanager.repository;

import com.example.taskmanager.model.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
} 