package com.example.FoodHub.repository;

import com.example.FoodHub.entity.RestaurantOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, Integer> {
}