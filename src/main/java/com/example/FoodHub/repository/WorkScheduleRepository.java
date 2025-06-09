package com.example.FoodHub.repository;

import com.example.FoodHub.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Integer> {
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.workDate BETWEEN :startDate AND :endDate")
    List<WorkSchedule> findByWeek(LocalDate startDate, LocalDate endDate);
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.workDate = :date")
    List<WorkSchedule> findByDate(LocalDate date);

}
