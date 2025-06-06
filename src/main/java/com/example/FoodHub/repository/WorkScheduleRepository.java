package com.example.FoodHub.repository;

import com.example.FoodHub.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Integer> {
    // Additional query methods can be defined here if needed
}
