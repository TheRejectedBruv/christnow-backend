package com.christnow.devotionals.repositories;

import com.christnow.devotionals.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 🔍 Find a user by their email
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}