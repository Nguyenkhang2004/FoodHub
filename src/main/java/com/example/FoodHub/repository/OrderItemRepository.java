package com.example.FoodHub.repository;

import com.example.FoodHub.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    // Custom query methods can be defined here if needed
}
