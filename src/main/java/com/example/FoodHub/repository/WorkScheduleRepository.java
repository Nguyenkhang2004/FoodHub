package com.example.FoodHub.repository;

import com.example.FoodHub.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Integer> {
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.workDate BETWEEN :startDate AND :endDate")
    List<WorkSchedule> findByWeek(LocalDate startDate, LocalDate endDate);
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.workDate = :date")
    List<WorkSchedule> findByDate(LocalDate date);
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.user.id = :userId AND ws.workDate >= :date")
    List<WorkSchedule> findByUserIdAndDateFromToday(@Param("userId") Integer userId, @Param("date") LocalDate date);
    @Query("SELECT w FROM WorkSchedule w " +
            "WHERE w.user.id = :userId " +
            "AND w.workDate = :today " +
            "AND :now >= w.startTime AND :now < w.endTime")
    Optional<WorkSchedule> findCurrentWorkShift(
            @Param("userId") Integer userId,
            @Param("today") LocalDate today,
            @Param("now") LocalTime now);

    List<WorkSchedule> findByUserId(Integer userId);
}
