package com.tourism.repository;

import com.tourism.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByEmailAndEnabled(String email, Boolean enabled);
}
