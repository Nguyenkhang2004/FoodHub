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
    Optional<Payment> findByOrderId(Integer orderId);
    List<Payment> findByStatus(String status);
    boolean existsByOrderIdAndStatus(Integer orderId, String status);
    Optional<Payment> findByOrderIdAndStatus(Integer orderId, String status);
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :start AND :end")

    List<Payment> findByCreatedAtBetween(Instant start, Instant end);

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
}