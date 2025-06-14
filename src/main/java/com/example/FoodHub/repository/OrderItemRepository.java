package com.example.FoodHub.repository;

import com.example.FoodHub.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    @Query("SELECT mi.name, SUM(oi.quantity), SUM(oi.quantity * oi.price) " +
            "FROM OrderItem oi " +
            "JOIN oi.menuItem mi " +
            "JOIN oi.order o " +
            "WHERE oi.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY mi.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopDishesByPeriod(@Param("start") Instant start, @Param("end") Instant end);
    List<OrderItem> findByOrderId(Integer orderId);
}