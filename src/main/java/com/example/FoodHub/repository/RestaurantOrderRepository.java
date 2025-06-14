package com.example.FoodHub.repository;

import com.example.FoodHub.entity.RestaurantOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, Integer>, JpaSpecificationExecutor<RestaurantOrder> {
    Page<RestaurantOrder> findByTableId(Integer tableId, Pageable pageable);
    List<RestaurantOrder> findByStatus(String status);
    Optional<RestaurantOrder> findFirstByTableIdAndStatusInOrderByCreatedAtDesc(Integer tableId, List<String> statuses);

    @Query("SELECT o FROM RestaurantOrder o " +
            "JOIN o.table t " +
            "WHERE t.area = :area " +
            "AND ((o.createdAt >= :currentShiftStart) " +
            "     OR (o.createdAt BETWEEN :previousShiftStart AND :previousShiftEnd " +
            "         AND o.status IN (:pendingStatuses))) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:tableNumber IS NULL OR t.tableNumber = :tableNumber) " +
            "AND (:minPrice IS NULL OR o.totalAmount >= :minPrice) " +
            "AND (:maxPrice IS NULL OR o.totalAmount <= :maxPrice) " +
            "AND (:startTime IS NULL OR o.createdAt >= :startTime)")
    Page<RestaurantOrder> findCurrentAndPreviousOrdersByArea(
            String area,
            List<String> pendingStatuses,
            LocalDateTime currentShiftStart,
            LocalDateTime previousShiftStart,
            LocalDateTime previousShiftEnd,
            String status,
            String tableNumber,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Instant startTime,
            Pageable pageable);

    @Query("SELECT SUM(ro.totalAmount) FROM RestaurantOrder ro WHERE ro.status = 'COMPLETED' AND ro.createdAt BETWEEN :start AND :end")
    Optional<BigDecimal> findTotalRevenueByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT COUNT(o) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end")
    Long countOrdersByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT HOUR(o.createdAt), SUM(o.totalAmount) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY HOUR(o.createdAt) ORDER BY HOUR(o.createdAt)")
    List<Object[]> findHourlyRevenueByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT DAYOFWEEK(o.createdAt), SUM(o.totalAmount) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY DAYOFWEEK(o.createdAt) ORDER BY DAYOFWEEK(o.createdAt)")
    List<Object[]> findDailyRevenueByPeriodForWeek(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT DAYOFMONTH(o.createdAt), SUM(o.totalAmount) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY DAYOFMONTH(o.createdAt) ORDER BY DAYOFMONTH(o.createdAt)")
    List<Object[]> findDailyRevenueByPeriodForMonth(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT MONTH(o.createdAt), SUM(o.totalAmount) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY MONTH(o.createdAt) ORDER BY MONTH(o.createdAt)")
    List<Object[]> findMonthlyRevenueByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM Payment p JOIN p.order o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY p.paymentMethod")
    List<Object[]> findRevenueByPaymentMethod(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT o.orderType, SUM(o.totalAmount) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY o.orderType")
    List<Object[]> findRevenueByOrderType(@Param("start") Instant start, @Param("end") Instant end);

    @Deprecated
    @Query("SELECT HOUR(o.createdAt), SUM(o.totalAmount) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY HOUR(o.createdAt)")
    List<Object[]> findDailyRevenueByPeriod(@Param("start") Instant start, @Param("end") Instant end);
}