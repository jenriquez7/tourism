package com.tourism.repository;

import com.tourism.model.Region;
import com.tourism.model.TouristicPlace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TouristicPlaceRepository extends JpaRepository<TouristicPlace, UUID> {

    Page<TouristicPlace> findByNameStartingWithIgnoreCase(String name, Pageable pageable);
    Page<TouristicPlace> findByRegion(Region region, Pageable pageable);
}
