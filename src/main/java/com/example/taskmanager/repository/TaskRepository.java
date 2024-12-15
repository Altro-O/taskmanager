package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByDeadlineBetween(LocalDateTime start, LocalDateTime end);
    List<Task> findByPriority(Priority priority);
    List<Task> findByCategory(Category category);
    List<Task> findByDeadlineBeforeAndCompletedFalse(LocalDateTime date);
    List<Task> findByTags(String tag);
    List<Task> findByUser(User user);
    
    @Query("SELECT t FROM Task t WHERE t.deadline >= :start AND t.deadline < :end")
    List<Task> findTodayTasks(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Task> findByUserAndDeadlineBetween(User user, LocalDateTime start, LocalDateTime end);

    List<Task> findByCompletedFalse();

    List<Task> findByCompleted(boolean completed);
    List<Task> findByUserAndCompleted(User user, boolean completed);
}
