package com.example.FoodHub.specification;

import com.example.FoodHub.entity.Category;
import com.example.FoodHub.entity.MenuItem;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuItemSpecification {
    public static Specification<MenuItem> buildSpecification(Integer categoryId, String normalizedKeyword, String status) {
        return (root, query, criteriaBuilder) -> {
            // Start with a true predicate (no filtering)
            List<Predicate> predicates = new ArrayList<>();

            // Filter by categoryId if provided
            if (categoryId != null) {
                Join<MenuItem, Category> categoryJoin = root.join("categories", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(categoryJoin.get("id"), categoryId));
            }

            // Filter by keyword if provided
            if (normalizedKeyword != null && !normalizedKeyword.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + normalizedKeyword.toLowerCase() + "%"
                ));
            }

            // Filter by status if provided
            if (status != null && !status.isEmpty()) {
                String normalizedStatus = status.toUpperCase();
                if (!Arrays.asList("AVAILABLE", "UNAVAILABLE").contains(normalizedStatus)) {
                    throw new AppException(ErrorCode.INVALID_KEY);
                }
                predicates.add(criteriaBuilder.equal(root.get("status"), normalizedStatus));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    public static Specification<MenuItem> hasCategory(Integer categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<MenuItem, Category> categoryJoin = root.join("categories", JoinType.LEFT);
            return criteriaBuilder.equal(categoryJoin.get("id"), categoryId);
        };
    }

    public static Specification<MenuItem> hasKeyword(String normalizedKeyword) {
        return (root, query, criteriaBuilder) ->
                normalizedKeyword == null || normalizedKeyword.isEmpty() ?
                        criteriaBuilder.conjunction() :
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                "%" + normalizedKeyword.toLowerCase() + "%"
                        );
    }

}
