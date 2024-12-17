package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class FirestoreTaskRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "tasks";

    public FirestoreTaskRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public Task save(Task task) {
        try {
            if (task.getId() == null) {
                task.setId(System.currentTimeMillis()); // Простая генерация ID
            }
            firestore.collection(COLLECTION_NAME)
                    .document(task.getId().toString())
                    .set(task)
                    .get();
            return task;
        } catch (Exception e) {
            throw new RuntimeException("Error saving task", e);
        }
    }

    public Optional<Task> findById(Long id) {
        try {
            var document = firestore.collection(COLLECTION_NAME)
                    .document(id.toString())
                    .get()
                    .get();
            
            if (document.exists()) {
                return Optional.of(document.toObject(Task.class));
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Error finding task", e);
        }
    }

    public List<Task> findByUser(User user) {
        try {
            return firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user.id", user.getId())
                    .get()
                    .get()
                    .getDocuments()
                    .stream()
                    .map(doc -> doc.toObject(Task.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error finding tasks for user", e);
        }
    }

    public List<Task> findByUserAndDeadlineToday(User user) {
        try {
            LocalDate today = LocalDate.now();
            return firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user.id", user.getId())
                    .whereGreaterThanOrEqualTo("deadline", today.atStartOfDay())
                    .whereLessThan("deadline", today.plusDays(1).atStartOfDay())
                    .get()
                    .get()
                    .getDocuments()
                    .stream()
                    .map(doc -> doc.toObject(Task.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error finding today's tasks", e);
        }
    }
}
