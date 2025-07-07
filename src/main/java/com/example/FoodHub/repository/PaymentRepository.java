package com.example.FoodHub.repository;

import com.example.FoodHub.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    boolean existsByOrderIdAndStatus(Integer orderId, String status);

    // Find by multiple statuses
    List<Payment> findByStatusIn(List<String> statuses);

    // Check if payment exists by order ID
    boolean existsByOrderId(Integer orderId);

    // Find by order ID and status
    Optional<Payment> findByOrderIdAndStatus(Integer orderId, String status);
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :start AND :end")

    List<Payment> findByCreatedAtBetween(Instant start, Instant end);

    // Find by createdAt between start and end with status filter
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :start AND :end AND (:status = 'all' OR p.status = :status)")
    List<Payment> findByCreatedAtBetweenAndStatus(Instant start, Instant end, String status);

    // Calculate total revenue by date for PAID status
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'PAID' AND DATE(p.createdAt) = :date")
    BigDecimal calculateTotalRevenueByDate(Instant date);
    Page<Payment> findByStatusInAndCreatedAtBefore(List<String> statuses, Instant createdAt, Pageable pageable);
    @Query("SELECT p FROM Payment p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "p.createdAt BETWEEN :start AND :end AND " +
            "(:transactionId IS NULL OR p.transactionId LIKE %:transactionId%)")
    Page<Payment> findByStatusAndCreatedAtBetween(
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("status") String status,
            @Param("transactionId") String transactionId,
            Pageable pageable);
    Optional<Payment> findByTransactionId(String transactionId);

    // Thống kê theo thời gian thanh toán thay vì thời gian đặt hàng
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'PAID' AND p.createdAt BETWEEN :start AND :end")
    Optional<BigDecimal> findTotalRevenueByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'PAID' AND p.createdAt BETWEEN :start AND :end")
    Long countPaidPaymentsByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT HOUR(p.createdAt), SUM(p.amount) FROM Payment p WHERE p.status = 'PAID' AND p.createdAt BETWEEN :start AND :end GROUP BY HOUR(p.createdAt) ORDER BY HOUR(p.createdAt)")
    List<Object[]> findHourlyRevenueByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT DAYOFWEEK(p.createdAt), SUM(p.amount) FROM Payment p WHERE p.status = 'PAID' AND p.createdAt BETWEEN :start AND :end GROUP BY DAYOFWEEK(p.createdAt) ORDER BY DAYOFWEEK(p.createdAt)")
    List<Object[]> findDailyRevenueByPeriodForWeek(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT DAYOFMONTH(p.createdAt), SUM(p.amount) FROM Payment p WHERE p.status = 'PAID' AND p.createdAt BETWEEN :start AND :end GROUP BY DAYOFMONTH(p.createdAt) ORDER BY DAYOFMONTH(p.createdAt)")
    List<Object[]> findDailyRevenueByPeriodForMonth(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT MONTH(p.createdAt), SUM(p.amount) FROM Payment p WHERE p.status = 'PAID' AND p.createdAt BETWEEN :start AND :end GROUP BY MONTH(p.createdAt) ORDER BY MONTH(p.createdAt)")
    List<Object[]> findMonthlyRevenueByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM Payment p WHERE p.status = 'PAID' AND p.createdAt BETWEEN :start AND :end GROUP BY p.paymentMethod")
    List<Object[]> findRevenueByPaymentMethod(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT o.orderType, SUM(p.amount) FROM Payment p JOIN p.order o WHERE p.status = 'PAID' AND p.createdAt BETWEEN :start AND :end GROUP BY o.orderType")
    List<Object[]> findRevenueByOrderType(@Param("start") Instant start, @Param("end") Instant end);

    // Find by status and createdAt before a given time
    List<Payment> findByStatusAndCreatedAtBefore(String status, Instant createdAt);

    // Search by transaction ID containing a substring
    List<Payment> findByTransactionIdContaining(String transactionId);

    // Autocomplete suggestions for order ID
    @Query("SELECT DISTINCT p FROM Payment p WHERE CAST(p.order.id AS string) LIKE %:query%")
    List<Payment> findSuggestionsByOrderId(@Param("query") String query);

    // Autocomplete suggestions for transaction ID
    @Query("SELECT DISTINCT p FROM Payment p WHERE p.transactionId LIKE %:query%")
    List<Payment> findSuggestionsByTransactionId(@Param("query") String query);

    List<Payment> findByStatusAndCreatedAtAfter(String status, Instant createdAt);

}