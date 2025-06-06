package com.example.FoodHub.repository;

import com.example.FoodHub.entity.RestaurantOrder;
import com.example.FoodHub.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, Integer> {

}
