package com.example.taskmanager.repository;

import com.example.taskmanager.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreUserRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "users";

    public FirestoreUserRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public User save(User user) {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(user.getEmail());
        try {
            docRef.set(user).get();
            return user;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    public Optional<User> findByEmail(String email) {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(email);
        try {
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return Optional.of(document.toObject(User.class));
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding user", e);
        }
    }

    public Optional<User> findByTelegramChatId(Long chatId) {
        try {
            var documents = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("telegramChatId", chatId)
                .get()
                .get()
                .getDocuments();

            if (!documents.isEmpty()) {
                return Optional.of(documents.get(0).toObject(User.class));
            }
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding user by telegram chat id", e);
        }
    }
}
