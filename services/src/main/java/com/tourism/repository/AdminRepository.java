package com.tourism.repository;

import com.tourism.model.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, UUID> {

    Page<Admin> findByEmailStartingWithIgnoreCase(String email, Pageable pageable);
}
