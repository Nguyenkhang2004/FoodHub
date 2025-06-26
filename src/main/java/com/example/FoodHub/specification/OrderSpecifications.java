package com.example.FoodHub.specification;

import com.example.FoodHub.entity.RestaurantOrder;
import com.example.FoodHub.enums.OrderStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class OrderSpecifications {
    public static Specification<RestaurantOrder> filterOrders(
            String status, Integer tableId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));

            if (tableId != null)
                predicates.add(cb.equal(root.get("table").get("id"), tableId));


            // Lọc đơn hàng TAKEAWAY hoặc DELIVERY phải có Payment là PAID và phương thức BANKING
            Predicate isTakeawayOrDelivery = root.get("orderType").in("TAKEAWAY", "DELIVERY");

            Join<Object, Object> paymentJoin = root.join("payment", JoinType.LEFT);

            Predicate paymentPaid = cb.equal(paymentJoin.get("status"), "PAID");
            Predicate paymentBanking = cb.equal(paymentJoin.get("paymentMethod"), "BANKING");

            Predicate requirePaidIfOnline = cb.or(
                    cb.not(isTakeawayOrDelivery), // nếu không phải TAKEAWAY hoặc DELIVERY thì cho qua
                    cb.and(isTakeawayOrDelivery, paymentPaid, paymentBanking) // nếu là thì phải PAID & BANKING
            );

            predicates.add(requirePaidIfOnline);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Hàm base - giữ nguyên
    public static Specification<RestaurantOrder> filterWorkShiftOrders(
            String status, String tableNumber, LocalDateTime startTime) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tableNumber != null) {
                predicates.add(cb.equal(root.get("table").get("tableNumber"), tableNumber));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (startTime != null) {
                Predicate afterStartTimeCondition = cb.or(
                        cb.greaterThanOrEqualTo(root.get("createdAt"), startTime),
                        cb.greaterThanOrEqualTo(root.get("updatedAt"), startTime)
                );

                Predicate beforeStartTimeCondition = cb.and(
                        cb.lessThan(root.get("createdAt"), startTime),
                        cb.lessThan(root.get("updatedAt"), startTime)
                );

                Predicate unfinishedCondition = cb.not(root.get("status").in(
                        OrderStatus.CANCELLED.name(),
                        OrderStatus.COMPLETED.name()
                ));

                Predicate unfinishedBeforeStartTime = cb.and(beforeStartTimeCondition, unfinishedCondition);

                predicates.add(cb.or(afterStartTimeCondition, unfinishedBeforeStartTime));
            } else {
                predicates.add(cb.not(root.get("status").in(
                        OrderStatus.CANCELLED.name(),
                        OrderStatus.COMPLETED.name()
                )));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Hàm lọc order cho waiter - sử dụng base và thêm area filter
    public static Specification<RestaurantOrder> filterWaiterOrders(
            String status, String tableNumber, String area, LocalDateTime startTime) {

        // Sử dụng base specification
        Specification<RestaurantOrder> baseSpec = filterWorkShiftOrders(status, tableNumber, startTime);

        // Thêm điều kiện area nếu có
        if (area != null) {
            Specification<RestaurantOrder> areaSpec = (root, query, cb) ->
                    cb.equal(root.get("table").get("area"), area);

            return baseSpec.and(areaSpec);
        }

        return baseSpec;
    }

    // Hàm lọc order cho chef - sử dụng base và thêm payment check
    public static Specification<RestaurantOrder> filterChefOrders(
            String status, String tableNumber, LocalDateTime startTime) {

        // Sử dụng base specification
        Specification<RestaurantOrder> baseSpec = filterWorkShiftOrders(status, tableNumber, startTime);

        // Thêm điều kiện payment cho TAKEAWAY/DELIVERY
        Specification<RestaurantOrder> paymentSpec = (root, query, cb) -> {
            Predicate isTakeawayOrDelivery = root.get("orderType").in("TAKEAWAY", "DELIVERY");

            Join<Object, Object> paymentJoin = root.join("payment", JoinType.LEFT);

            Predicate paymentPaid = cb.equal(paymentJoin.get("status"), "PAID");
            Predicate paymentBanking = cb.equal(paymentJoin.get("paymentMethod"), "BANKING");

            return cb.or(
                    cb.not(isTakeawayOrDelivery), // nếu không phải TAKEAWAY hoặc DELIVERY thì cho qua
                    cb.and(isTakeawayOrDelivery, paymentPaid, paymentBanking) // nếu là thì phải PAID & BANKING
            );
        };

        return baseSpec.and(paymentSpec);
    }

}
