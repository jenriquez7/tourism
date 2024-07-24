package com.tourism.repository;

import com.tourism.model.Tourist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TouristRepository extends JpaRepository<Tourist, UUID> {
    Page<Tourist> findByEmailStartingWithIgnoreCase(String email, Pageable pageable);
    Page<Tourist> findByLastNameStartingWithIgnoreCase(String lastName, Pageable pageable);
}
