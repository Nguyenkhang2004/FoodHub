package com.example.FoodHub.repository;

import com.example.FoodHub.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface


PaymentRepository extends JpaRepository<Payment, Integer> {
    // Sửa lại findByOrderId (dựa trên order.id)
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId")
    Optional<Payment> findByOrderId(@Param("orderId") Integer orderId);

    // Thêm phương thức findByStatus để lọc theo một trạng thái
    List<Payment> findByStatus(String status);

    // Lọc theo status (hỗ trợ nhiều trạng thái)
    List<Payment> findByStatusIn(List<String> statuses);

    // Lọc theo khoảng thời gian
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :start AND :end")
    List<Payment> findByCreatedAtBetween(Instant start, Instant end);

    // Tính tổng doanh thu theo ngày
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'PAID' AND DATE(p.createdAt) = :date")
    BigDecimal calculateTotalRevenueByDate(Instant date);

    // Lọc theo khoảng thời gian và trạng thái
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :start AND :end AND (:status = 'all' OR p.status = :status)")
    List<Payment> findByCreatedAtBetweenAndStatus(Instant start, Instant end, String status);

    // Search theo transaction_id
    List<Payment> findByTransactionIdContaining(String transactionId);

    // Autocomplete cho orderId (sử dụng CAST để chuyển Integer thành String)
    @Query("SELECT DISTINCT p FROM Payment p WHERE CAST(p.order.id AS string) LIKE %:query%")
    List<Payment> findSuggestionsByOrderId(@Param("query") String query);

    // Autocomplete cho transactionId
    @Query("SELECT DISTINCT p FROM Payment p WHERE p.transactionId LIKE %:query%")
    List<Payment> findSuggestionsByTransactionId(@Param("query") String query);
}