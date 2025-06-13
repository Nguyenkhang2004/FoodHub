package com.example.FoodHub.repository;

import com.example.FoodHub.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrderId(Integer orderId);

    @Query("SELECT SUM(oi.quantity * oi.price) FROM OrderItem oi WHERE oi.order.id = :orderId")
    BigDecimal calculateTotalAmountByOrderId(Integer orderId);
}