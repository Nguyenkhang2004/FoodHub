package com.example.FoodHub.repository;

import com.example.FoodHub.entity.RestaurantOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<RestaurantOrder, Integer> {
    // Custom query methods can be defined here if needed
}
