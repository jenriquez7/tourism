package com.tourism.repository;

import com.tourism.model.RefreshToken;
import com.tourism.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    RefreshToken findByToken(String token);
    void deleteByToken(String token);
    void deleteByUser(User user);
}
