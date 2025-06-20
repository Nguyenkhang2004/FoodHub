package com.example.FoodHub.specification;

import com.example.FoodHub.entity.RestaurantTable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class    TableSpecifications {
    public static Specification<RestaurantTable> filterTables(String tableNumber, String status, String area) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tableNumber != null && !tableNumber.isEmpty()) {
                predicates.add(cb.equal(root.get("tableNumber"), tableNumber));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (area != null && !area.isEmpty()) {
                predicates.add(cb.equal(root.get("area"), area));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
