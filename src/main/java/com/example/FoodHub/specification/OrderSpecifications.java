package com.example.FoodHub.specification;

import com.example.FoodHub.entity.RestaurantOrder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
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
}
