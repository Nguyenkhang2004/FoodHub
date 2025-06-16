package com.example.FoodHub.repository;

import com.example.FoodHub.entity.QrScanLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QrScanLogRepository extends JpaRepository<QrScanLog, Integer> {
}
