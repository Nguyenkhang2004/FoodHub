package com.example.FoodHub.repository;

import com.example.FoodHub.entity.InvalidateToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvalidateTokenRepository extends JpaRepository<InvalidateToken, String> {
    List<InvalidateToken> findByExpiryTimeBefore(Instant now);
    boolean existsByToken(String jwtId);
//    boolean existsByJwtId(String jwtId);
    @Modifying
    @Query("DELETE FROM InvalidateToken i WHERE i.expiryTime < :now")
    void deleteExpiredTokens(@Param("now") Instant now);
}
