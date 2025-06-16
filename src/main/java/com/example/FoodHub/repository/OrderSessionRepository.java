package com.example.FoodHub.repository;

import com.example.FoodHub.entity.OrderSession;
import com.example.FoodHub.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface OrderSessionRepository extends JpaRepository<OrderSession, Integer> {
    Optional<OrderSession> findBySessionTokenAndStatus(String sessionToken, String status);
    Optional<OrderSession> findByTableIdAndStatus(Integer tableId, String status);
}
