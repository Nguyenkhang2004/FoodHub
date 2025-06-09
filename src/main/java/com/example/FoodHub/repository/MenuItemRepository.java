package com.example.FoodHub.repository;

import com.example.FoodHub.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
    Page<MenuItem> findAll(Pageable pageable);

    Page<MenuItem> findByCategoriesNameIgnoreCase(String categoryName, Pageable pageable);

    Page<MenuItem> findByStatusIgnoreCase(String status, Pageable pageable);
}

