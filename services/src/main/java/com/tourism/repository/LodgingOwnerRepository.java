package com.tourism.repository;

import com.tourism.model.LodgingOwner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LodgingOwnerRepository extends JpaRepository<LodgingOwner, UUID> {

    Page<LodgingOwner> findByEmailStartingWithIgnoreCase(String email, Pageable pageable);
    Page<LodgingOwner> findByLastNameStartingWithIgnoreCase(String lastName, Pageable pageable);
}
