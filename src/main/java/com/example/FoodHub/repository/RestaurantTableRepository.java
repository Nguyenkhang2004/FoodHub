package com.example.FoodHub.repository;

import com.example.FoodHub.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Integer>, JpaSpecificationExecutor<RestaurantTable> {
    // Custom query methods can be defined here if needed
    List<RestaurantTable> findByStatus(String status);
    Optional<RestaurantTable> findByTableNumber(String TableNumber);
    List<RestaurantTable> findByArea(String area);
    List<RestaurantTable> findByAreaAndStatus(String area, String status);

}
