package com.example.FoodHub.specification;

import com.example.FoodHub.entity.RestaurantOrder;
import com.example.FoodHub.enums.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class OrderSpecifications {
    public static Specification<RestaurantOrder> filterOrders(
            String status, Integer tableId, BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (tableId != null) predicates.add(cb.equal(root.get("table").get("id"), tableId));
            if (minPrice != null) predicates.add(cb.ge(root.get("totalAmount"), minPrice));
            if (maxPrice != null) predicates.add(cb.le(root.get("totalAmount"), maxPrice));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    public static Specification<RestaurantOrder> filterWorkShiftOrders(
            String area, String status, String tableNumber, BigDecimal minPrice, BigDecimal maxPrice,
            LocalDateTime startTime) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by area
            if (area != null) {
                predicates.add(cb.equal(root.get("table").get("area"), area));
            }

            // Filter by tableNumber if provided
            if (tableNumber != null) {
                predicates.add(cb.equal(root.get("table").get("tableNumber"), tableNumber));
            }

            // Filter by price range
            if (minPrice != null) predicates.add(cb.ge(root.get("totalAmount"), minPrice));
            if (maxPrice != null) predicates.add(cb.le(root.get("totalAmount"), maxPrice));

            // Main logic: Orders created/updated after startTime OR unfinished orders
            if (startTime != null) {
                // Điều kiện 1: Đơn hàng được tạo hoặc cập nhật sau startTime
                Predicate timeCondition = cb.or(
                        cb.greaterThanOrEqualTo(root.get("createdAt"), startTime),
                        cb.greaterThanOrEqualTo(root.get("updatedAt"), startTime)
                );

                // Điều kiện 2: Đơn hàng chưa hoàn thành (không phải CANCELLED hoặc COMPLETED)
                Predicate unfinishedCondition = cb.not(root.get("status").in(
                        OrderStatus.CANCELLED.name(),
                        OrderStatus.COMPLETED.name()
                ));

                // Kết hợp 2 điều kiện bằng OR
                predicates.add(cb.or(timeCondition, unfinishedCondition));
            } else {
                // Nếu không có startTime, chỉ lấy đơn hàng chưa hoàn thành
                predicates.add(cb.not(root.get("status").in(
                        OrderStatus.CANCELLED.name(),
                        OrderStatus.COMPLETED.name()
                )));
            }

            // Filter by specific status if provided (this will override the above status logic)
            if (status != null) {
                // Nếu có status cụ thể, thêm điều kiện này
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
