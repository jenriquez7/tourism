package com.tourism.repository;

import com.tourism.model.Login;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoginRepository extends JpaRepository<Login, UUID> {
}
