package com.example.taskmanager.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FirebaseAuthService {
    
    public void createUser(String email, String password) {
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setEmailVerified(false);
                
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            log.info("Successfully created Firebase user: {}", userRecord.getUid());
        } catch (Exception e) {
            log.error("Error creating Firebase user", e);
            throw new RuntimeException("Ошибка при создании пользователя", e);
        }
    }
    
    public boolean verifyUser(String email, String password) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            return userRecord != null && userRecord.isEmailVerified();
        } catch (Exception e) {
            log.error("Error verifying user", e);
            return false;
        }
    }
} 