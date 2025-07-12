package com.example.FoodHub.repository;

import com.example.FoodHub.entity.User;
import com.example.FoodHub.entity.WorkSchedule;
import com.example.FoodHub.entity.WorkShiftLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkShiftLogRepository extends JpaRepository<WorkShiftLog, Integer> {
    Optional<WorkShiftLog> findByUserIdAndWorkScheduleId(Integer userId, Integer workScheduleId);
    boolean existsByWorkSchedule(WorkSchedule schedule);
    boolean existsByUserIdAndWorkScheduleId(Integer userId, Integer workScheduleId);
    Optional<WorkShiftLog> findByWorkScheduleId(Integer workScheduleId);
    List<WorkShiftLog> findByUserId(Integer userId);
}
