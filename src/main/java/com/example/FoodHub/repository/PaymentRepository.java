package com.example.FoodHub.repository;

import com.example.FoodHub.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByOrderId(Integer orderId);
    List<Payment> findByStatus(String status);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :start AND :end")

    List<Payment> findByCreatedAtBetween(Instant start, Instant end);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'PAID' AND DATE(p.createdAt) = :date")
    BigDecimal calculateTotalRevenueByDate(Instant date);
}