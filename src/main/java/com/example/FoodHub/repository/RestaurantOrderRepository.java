package com.example.FoodHub.repository;

import com.example.FoodHub.entity.RestaurantOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
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

    @Query("SELECT o FROM RestaurantOrder o " +
            "WHERE o.status = :status " +
            "AND o.createdAt BETWEEN :start AND :end " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(COALESCE(o.user.username, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "CAST(o.id AS string) LIKE CONCAT('%', :search, '%'))")
    Page<RestaurantOrder> findByStatusAndCreatedAtBetween(
            @Param("status") String status,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("search") String search,
            Pageable pageable);

    // SỬA QUERY ĐỂ TƯƠNG THÍCH VỚI MÚI GIỜ
    @Query(value = "SELECT DATE(o.created_at) as date, " +
            "COUNT(o.id) as count " +
            "FROM restaurant_order o " +
            "WHERE o.status = :status " +
            "AND o.created_at BETWEEN :start AND :end " +
            "GROUP BY DATE(o.created_at) " +
            "ORDER BY date", nativeQuery = true)
    List<Object[]> countOrdersByDateMySQL(
            @Param("status") String status,
            @Param("start") Instant start,
            @Param("end") Instant end);

//    @Query("SELECT o FROM RestaurantOrder o " +
//            "LEFT JOIN o.payment p " +
//            "WHERE o.status = 'COMPLETED' " +
//            "AND o.createdAt BETWEEN :start AND :end " +
//            "AND (:orderType IS NULL OR :orderType = '' OR o.orderType = :orderType) " +
//            "AND (:minPrice IS NULL OR o.totalAmount >= :minPrice) " +
//            "AND (:maxPrice IS NULL OR o.totalAmount <= :maxPrice) " +
//            "AND (:paymentMethod IS NULL OR :paymentMethod = '' OR p.paymentMethod = :paymentMethod) " +
//            "AND (:search IS NULL OR :search = '' OR " +
//            "CAST(o.id AS string) = :search OR " +
//            "LOWER(o.user.username) LIKE LOWER(CONCAT('%', :search, '%'))) " +
//            "GROUP BY o.id")
//    Page<RestaurantOrder> findCompletedOrdersFiltered(
//            @Param("start") Instant start,
//            @Param("end") Instant end,
//            @Param("orderType") String orderType,
//            @Param("minPrice") BigDecimal minPrice,
//            @Param("maxPrice") BigDecimal maxPrice,
//            @Param("paymentMethod") String paymentMethod,
//            @Param("search") String search,
//            Pageable pageable);
@Query("SELECT o FROM RestaurantOrder o " +
        "LEFT JOIN o.payment p " +
        "LEFT JOIN o.user u " +
        "WHERE (:status IS NULL OR o.status = :status) " +
        "AND o.createdAt BETWEEN :start AND :end " +
        "AND (:orderType IS NULL OR :orderType = '' OR o.orderType = :orderType) " +
        "AND (:minPrice IS NULL OR o.totalAmount >= :minPrice) " +
        "AND (:maxPrice IS NULL OR o.totalAmount <= :maxPrice) " +
        "AND (:paymentMethod IS NULL OR :paymentMethod = '' OR p.paymentMethod = :paymentMethod) " +
        "AND (:search IS NULL OR :search = '' OR " +
        "CAST(o.id AS string) = :search OR " +
        "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))) " +
        "GROUP BY o.id")
Page<RestaurantOrder> findOrdersFiltered(
        @Param("status") String status,
        @Param("start") Instant start,
        @Param("end") Instant end,
        @Param("orderType") String orderType,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("paymentMethod") String paymentMethod,
        @Param("search") String search,
        Pageable pageable);
    Page<RestaurantOrder> findByUserId(@Param("userId") Integer userId, Pageable pageable);
    Optional<RestaurantOrder> findFirstByTableIdAndStatusIn(Integer tableId, List<String> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM RestaurantOrder o WHERE o.id = :id")
    Optional<RestaurantOrder> findByIdWithLock(Integer id);
    // 1. Trả về ID đơn hàng đang hoạt động nếu có
    @Query("""
        SELECT r.id FROM RestaurantOrder r
        WHERE r.table.id = :tableId
          AND r.status IN ('PENDING', 'PREPARING', 'READY', 'CONFIRMED')
        ORDER BY r.createdAt ASC
        LIMIT 1
    """)
    Integer findActiveOrderIdByTable(@Param("tableId") Integer tableId);

    // 2. Kiểm tra xem có đơn hàng hoạt động nào không
    @Query("""
        SELECT COUNT(r) > 0 FROM RestaurantOrder r
        WHERE r.table.id = :tableId
          AND r.status IN ('PENDING', 'PREPARING', 'READY', 'CONFIRMED')
    """)
    boolean hasActiveOrder(@Param("tableId") Integer tableId);
}