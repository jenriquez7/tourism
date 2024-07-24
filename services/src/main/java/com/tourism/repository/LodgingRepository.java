package com.tourism.repository;

import com.tourism.model.Lodging;
import com.tourism.model.TouristicPlace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LodgingRepository extends JpaRepository<Lodging, UUID> {
    
    Page<Lodging> findByTouristicPlace(TouristicPlace touristicPlace, Pageable pageable);

}
