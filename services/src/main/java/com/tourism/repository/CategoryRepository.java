package com.tourism.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tourism.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

   Page<Category> findByNameStartingWithIgnoreCaseOrderByNameAsc(String name, Pageable pageable);

   Page<Category> findAllByOrderByNameAsc(Pageable pageable);

}
