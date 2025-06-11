package com.example.FoodHub.repository;

import com.example.FoodHub.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {
    // Additional query methods can be defined here if needed

}
