package com.example.FoodHub.repository;

import com.example.FoodHub.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
    List<MenuItem> findByCategoriesNameIgnoreCase(String name);
    List<MenuItem> findByStatusIgnoreCase(String status);
}

