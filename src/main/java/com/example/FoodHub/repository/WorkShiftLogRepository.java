package com.example.FoodHub.repository;

import com.example.FoodHub.entity.User;
import com.example.FoodHub.entity.WorkSchedule;
import com.example.FoodHub.entity.WorkShiftLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkShiftLogRepository extends JpaRepository<WorkShiftLog, Integer> {
    Optional<WorkShiftLog> findByUserAndWorkSchedule(User user, WorkSchedule workSchedule);
    boolean existsByWorkSchedule(WorkSchedule schedule);

}
