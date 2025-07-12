package com.example.FoodHub.repository;

import com.example.FoodHub.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    // Find by order ID, referencing order.id
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId")
    Optional<Payment> findByOrderId(@Param("orderId") Integer orderId);

    // Find by status
    List<Payment> findByStatus(String status);

    // Find by multiple statuses
    List<Payment> findByStatusIn(List<String> statuses);

    // Check if payment exists by order ID
    boolean existsByOrderId(Integer orderId);

    // Find by order ID and status
    Optional<Payment> findByOrderIdAndStatus(Integer orderId, String status);

    // Find by createdAt between start and end
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :start AND :end")
    List<Payment> findByCreatedAtBetween(Instant start, Instant end);

    // Find by status and createdAt before a given time
    List<Payment> findByStatusAndCreatedAtBefore(String status, Instant createdAt);


//========================================================================================

    @Query("SELECT p FROM Payment p WHERE p.transactionId LIKE %:transactionId% AND p.createdAt BETWEEN :start AND :end")
    List<Payment> findByTransactionIdContainingAndCreatedAtBetween(String transactionId, Instant start, Instant end);

}