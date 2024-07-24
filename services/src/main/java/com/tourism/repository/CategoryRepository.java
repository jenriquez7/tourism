package com.tourism.repository;

import com.tourism.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Category[] findByNameStartingWithIgnoreCaseOrderByNameAsc(String name);

    Category[] findAllByOrderByNameAsc();

}
