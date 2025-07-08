package com.example.FoodHub.scheduler;

import com.example.FoodHub.entity.WorkSchedule;
import com.example.FoodHub.entity.WorkShiftLog;
import com.example.FoodHub.enums.ShiftStatus;
import com.example.FoodHub.repository.WorkScheduleRepository;
import com.example.FoodHub.repository.WorkShiftLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkShiftLogScheduler {
    private final WorkShiftLogRepository workShiftLogRepository;
    private final WorkScheduleRepository workScheduleRepository;
    @Scheduled(cron = "0 30 12 * * *") // 12:30 PM mỗi ngày
    public void markMorningShiftAbsentees() {
        markAbsentForShiftType("MORNING");
    }

    @Scheduled(cron = "0 30 17 * * *") // 17:30 PM mỗi ngày
    public void markAfternoonShiftAbsentees() {
        markAbsentForShiftType("AFTERNOON");
    }

    @Scheduled(cron = "0 0 23 * * *") // 23:00 PM mỗi ngày
    public void markEveningShiftAbsentees() {
        markAbsentForShiftType("EVENING");
    }

    @Transactional
    public void markAbsentForShiftType(String shiftType) {
        LocalDate today = LocalDate.now();
        List<WorkSchedule> schedules = workScheduleRepository.findAllByWorkDateAndShiftType(today, shiftType);

        for (WorkSchedule schedule : schedules) {
            boolean hasCheckedIn = workShiftLogRepository.existsByWorkSchedule(schedule);
            if (!hasCheckedIn) {
                WorkShiftLog shiftLog = WorkShiftLog.builder()
                        .user(schedule.getUser())
                        .workSchedule(schedule)
                        .checkInTime(null)
                        .checkOutTime(null)
                        .status(ShiftStatus.ABSENT.name())
                        .build();
                workShiftLogRepository.save(shiftLog);
                log.info("Marked ABSENT for user {} in {} shift", schedule.getUser().getId(), shiftType);
            }
        }
    }

}
