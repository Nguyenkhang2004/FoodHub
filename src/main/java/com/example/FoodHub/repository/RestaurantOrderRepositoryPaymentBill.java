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

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantOrderRepositoryPaymentBill extends JpaRepository<RestaurantOrder, Integer>, JpaSpecificationExecutor<RestaurantOrder> {
    //kiểm tra xem có đơn hàng nào với table_id cụ thể và status nằm trong danh sách statuses không
    boolean existsByTableIdAndStatusIn(Integer tableId, List<String> statuses);

    Page<RestaurantOrder> findByTableId(Integer tableId, Pageable pageable);
    List<RestaurantOrder> findByStatus(String status);
    Optional<RestaurantOrder> findFirstByTableIdAndStatusInOrderByCreatedAtDesc(Integer tableId, List<String> statuses);
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