package com.example.FoodHub.repository;

import com.example.FoodHub.entity.RestaurantOrder;
import com.example.FoodHub.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    // Kiểm tra xem có đơn hàng nào với table_id cụ thể và status nằm trong danh sách statuses không
    boolean existsByTableIdAndStatusIn(Integer tableId, List<String> statuses);

    // Tìm tất cả đơn hàng theo tableId với phân trang
    Page<RestaurantOrder> findByTableId(Integer tableId, Pageable pageable);

    // Tìm tất cả đơn hàng theo status
    List<RestaurantOrder> findByStatus(String status);

    // Tìm đơn hàng đầu tiên theo tableId và status trong danh sách statuses, sắp xếp theo createdAt giảm dần
    Optional<RestaurantOrder> findFirstByTableIdAndStatusInOrderByCreatedAtDesc(Integer tableId, List<String> statuses);

    // Tìm tổng doanh thu trong khoảng thời gian với status COMPLETED
    @Query("SELECT SUM(ro.totalAmount) FROM RestaurantOrder ro WHERE ro.status = 'COMPLETED' AND ro.createdAt BETWEEN :start AND :end")
    Optional<BigDecimal> findTotalRevenueByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    // Đếm số đơn hàng hoàn thành trong khoảng thời gian
    @Query("SELECT COUNT(o) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end")
    Long countOrdersByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    // Tìm doanh thu theo giờ trong khoảng thời gian
    @Query("SELECT HOUR(o.createdAt), SUM(o.totalAmount) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY HOUR(o.createdAt) ORDER BY HOUR(o.createdAt)")
    List<Object[]> findHourlyRevenueByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    // Tìm doanh thu theo ngày trong tuần
    @Query("SELECT DAYOFWEEK(o.createdAt), SUM(o.totalAmount) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY DAYOFWEEK(o.createdAt) ORDER BY DAYOFWEEK(o.createdAt)")
    List<Object[]> findDailyRevenueByPeriodForWeek(@Param("start") Instant start, @Param("end") Instant end);

    // Tìm doanh thu theo ngày trong tháng
    @Query("SELECT DAYOFMONTH(o.createdAt), SUM(o.totalAmount) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY DAYOFMONTH(o.createdAt) ORDER BY DAYOFMONTH(o.createdAt)")
    List<Object[]> findDailyRevenueByPeriodForMonth(@Param("start") Instant start, @Param("end") Instant end);

    // Tìm doanh thu theo tháng
    @Query("SELECT MONTH(o.createdAt), SUM(o.totalAmount) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY MONTH(o.createdAt) ORDER BY MONTH(o.createdAt)")
    List<Object[]> findMonthlyRevenueByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    // Tìm doanh thu theo phương thức thanh toán
    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM Payment p JOIN p.order o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY p.paymentMethod")
    List<Object[]> findRevenueByPaymentMethod(@Param("start") Instant start, @Param("end") Instant end);

    // Tìm doanh thu theo loại đơn hàng
    @Query("SELECT o.orderType, SUM(o.totalAmount) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY o.orderType")
    List<Object[]> findRevenueByOrderType(@Param("start") Instant start, @Param("end") Instant end);

    // Query bị deprecated (giữ nguyên để tránh lỗi khi migrate)
    @Deprecated
    @Query("SELECT HOUR(o.createdAt), SUM(o.totalAmount) FROM RestaurantOrder o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :start AND :end GROUP BY HOUR(o.createdAt)")
    List<Object[]> findDailyRevenueByPeriod(@Param("start") Instant start, @Param("end") Instant end);

    // Tìm đơn hàng hiện tại và trước đó theo khu vực
    @Query("SELECT o FROM RestaurantOrder o JOIN o.table t WHERE t.area = :area " +
            "AND ((o.createdAt >= :currentShiftStart) " +
            "     OR (o.createdAt BETWEEN :previousShiftStart AND :previousShiftEnd " +
            "         AND o.status IN :pendingStatuses)) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:tableNumber IS NULL OR t.tableNumber = :tableNumber) " +
            "AND (:startTime IS NULL OR o.createdAt >= :startTime)")
    Page<RestaurantOrder> findCurrentAndPreviousOrdersByArea(
            @Param("area") String area,
            @Param("pendingStatuses") List<String> pendingStatuses,
            @Param("currentShiftStart") LocalDateTime currentShiftStart,
            @Param("previousShiftStart") LocalDateTime previousShiftStart,
            @Param("previousShiftEnd") LocalDateTime previousShiftEnd,
            @Param("status") String status,
            @Param("tableNumber") String tableNumber,
            @Param("startTime") Instant startTime,
            Pageable pageable);
}

