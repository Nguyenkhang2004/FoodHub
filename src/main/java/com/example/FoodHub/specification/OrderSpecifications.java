package com.example.FoodHub.specification;

import com.example.FoodHub.entity.RestaurantOrder;
import com.example.FoodHub.enums.OrderStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OrderSpecifications {
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
            String status, String tableNumber, Instant startTime, Instant endTime) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Payment filtering logic - exclude TAKEAWAY/DELIVERY orders that are BANKING and not PAID
            Predicate isTakeawayOrDelivery = root.get("orderType").in("TAKEAWAY", "DELIVERY");
            Join<Object, Object> paymentJoin = root.join("payment", JoinType.LEFT);
            Predicate paymentBanking = cb.equal(paymentJoin.get("paymentMethod"), "BANKING");
            Predicate paymentNotPaid = cb.notEqual(paymentJoin.get("status"), "PAID");

            // Exclude TAKEAWAY/DELIVERY orders that are BANKING and not PAID
            Predicate excludeBankingNotPaid = cb.and(isTakeawayOrDelivery, paymentBanking, paymentNotPaid);
            // Include all orders except those that match the exclude condition
            predicates.add(cb.not(excludeBankingNotPaid));

            if (tableNumber != null) {
                predicates.add(cb.equal(root.get("table").get("tableNumber"), tableNumber));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (startTime != null) {
                Predicate beforeStartTimeCondition = cb.or(
                        cb.lessThan(root.get("createdAt"), startTime),
                        cb.lessThan(root.get("updatedAt"), startTime)
                );

                Predicate unfinishedCondition = cb.not(root.get("status").in(
                        OrderStatus.CANCELLED.name(),
                        OrderStatus.COMPLETED.name()
                ));

                Predicate unfinishedBeforeStartTime = cb.and(beforeStartTimeCondition, unfinishedCondition);

                Predicate withinTimeRangeCondition;
                if (endTime != null) {
                    withinTimeRangeCondition = cb.or(
                            cb.and(
                                    cb.greaterThanOrEqualTo(root.get("createdAt"), startTime),
                                    cb.lessThanOrEqualTo(root.get("createdAt"), endTime)
                            ),
                            cb.and(
                                    cb.greaterThanOrEqualTo(root.get("updatedAt"), startTime),
                                    cb.lessThanOrEqualTo(root.get("updatedAt"), endTime)
                            )
                    );
                } else {
                    withinTimeRangeCondition = cb.or(
                            cb.greaterThanOrEqualTo(root.get("createdAt"), startTime),
                            cb.greaterThanOrEqualTo(root.get("updatedAt"), startTime)
                    );
                }

                predicates.add(cb.or(unfinishedBeforeStartTime, withinTimeRangeCondition));

            } else {
                log.info("No startTime provided, filtering unfinished orders only");
                predicates.add(cb.not(root.get("status").in(
                        OrderStatus.CANCELLED.name(),
                        OrderStatus.COMPLETED.name()
                )));
            }

            if (!Long.class.equals(query.getResultType())) {
                query.orderBy(cb.desc(
                        cb.coalesce(root.get("updatedAt"), root.get("createdAt"))
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }



//    // Hàm lọc order cho waiter - sử dụng base và thêm area filter
//    public static Specification<RestaurantOrder> filterWaiterOrders(
//            String status, String tableNumber, String area, Instant startTime, Instant endTime) {
//
//        log.info("Filtering waiter orders with status: {}, tableNumber: {}, area: {}, startTime: {}, endTime: {}",
//                status, tableNumber, area, startTime, endTime);
//
//        // Bộ lọc cơ bản (status, bàn, thời gian)
//        Specification<RestaurantOrder> baseSpec = filterWorkShiftOrders(status, tableNumber, startTime, endTime);
//
//        return (root, query, cb) -> {
//            Predicate basePredicate = baseSpec.toPredicate(root, query, cb);
//
//            // Lọc DINE_IN theo area nếu có
//            Predicate dineInFilter = cb.and(
//                    cb.equal(root.get("orderType"), "DINE_IN"),
//                    cb.isNotNull(root.get("table")),
//                    (area == null || area.isEmpty())
//                            ? cb.conjunction() // không lọc theo area
//                            : cb.equal(root.get("table").get("area"), area)
//            );
//
//            // TAKEAWAY/DELIVERY không cần lọc theo khu vực
//            Predicate otherTypes = root.get("orderType").in("TAKEAWAY", "DELIVERY");
//
//            // Combine điều kiện
//            return cb.and(basePredicate, cb.or(dineInFilter, otherTypes));
//        };
//    }
//
//
//    // Hàm lọc order cho chef - sử dụng base và thêm payment check
//    public static Specification<RestaurantOrder> filterChefOrders(
//            String status, String tableNumber, Instant startTime, Instant endTime) {
//        log.info("Filtering chef orders with status: {}, tableNumber: {}, startTime: {}, endTime: {}",
//                status, tableNumber, startTime, endTime);
//
//
//        return filterWorkShiftOrders(status, tableNumber, startTime, endTime);
//    }

}
