package com.example.FoodHub.repository;

import com.example.FoodHub.entity.InvalidateToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidateTokenRepository extends JpaRepository<InvalidateToken, String> {
    // This interface extends JpaRepository to provide CRUD operations for InvalidateToken entities.
    // You can add custom query methods if needed.
}
