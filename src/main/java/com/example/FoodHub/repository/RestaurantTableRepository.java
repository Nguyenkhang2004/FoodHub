package com.example.FoodHub.repository;

import com.example.FoodHub.entity.RestaurantTable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Integer>, JpaSpecificationExecutor<RestaurantTable> {
    // Custom query methods can be defined here if needed
    List<RestaurantTable> findByStatus(String status);
    Optional<RestaurantTable> findByTableNumber(String tableNumber);
    List<RestaurantTable> findByArea(String area);
    List<RestaurantTable> findByAreaAndStatus(String area, String status);
    Optional<RestaurantTable> findByQrCode(String qrCode);
    Optional<RestaurantTable> findByCurrentToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RestaurantTable t WHERE t.qrCode = :qrCode")
    Optional<RestaurantTable> findByQrCodeWithLock(@Param("qrCode") String qrCode);

    // Trong RestaurantTableRepository
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RestaurantTable t WHERE t.id = :id")
    Optional<RestaurantTable> findByIdWithLock(@Param("id") Integer id);

    List<RestaurantTable> findByTokenExpiryBefore(Instant instant);


}
