package com.example.FoodHub.repository;

import com.example.FoodHub.entity.RestaurantOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, Integer> {
    List<RestaurantOrder> findByTableId(Integer tableId);
    List<RestaurantOrder> findByStatus(String status);
}
