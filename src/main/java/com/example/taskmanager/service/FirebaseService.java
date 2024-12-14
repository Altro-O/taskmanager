package com.example.taskmanager.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.example.taskmanager.model.Task;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseService {
    
    public void syncTask(Task task) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            db.collection("tasks")
                .document(task.getId().toString())
                .set(task);
        } catch (Exception e) {
            System.err.println("Ошибка синхронизации с Firebase: " + e.getMessage());
        }
    }
    
    public void deleteTask(Long taskId) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            db.collection("tasks")
                .document(taskId.toString())
                .delete();
        } catch (Exception e) {
            System.err.println("Ошибка удаления из Firebase: " + e.getMessage());
        }
    }

    public void checkSync() {
        try {
            Firestore db = FirestoreClient.getFirestore();
            db.collection("tasks").get().get().forEach(doc -> {
                System.out.println("Синхронизированная задача: " + doc.getId());
            });
        } catch (Exception e) {
            System.err.println("Ошибка проверки синхронизации: " + e.getMessage());
        }
    }
} 