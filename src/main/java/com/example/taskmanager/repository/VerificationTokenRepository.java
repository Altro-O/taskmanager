package com.example.taskmanager.repository;

import com.example.taskmanager.model.User;
import com.example.taskmanager.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByUser(User user);
    Optional<VerificationToken> findByCode(String code);
} 