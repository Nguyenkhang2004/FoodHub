package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.ShiftRequest;
import com.example.FoodHub.dto.response.ShiftResponse;
import com.example.FoodHub.dto.request.EmployeeWorkRequest;
import com.example.FoodHub.dto.response.EmployeeWorkResponse;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.entity.WorkSchedule;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.repository.UserRepository;
import com.example.FoodHub.repository.WorkScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkScheduleService {
    @Autowired
    private WorkScheduleRepository workScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ShiftResponse> getShiftsForWeek(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<WorkSchedule> schedules = workScheduleRepository.findByWeek(weekStart, weekEnd);
        return schedules.stream().map(this::convertToShiftResponse).collect(Collectors.toList());
    }

    public List<EmployeeWorkResponse> getEmployeesByRole(String role) {
        List<User> users = userRepository.findByRoleName_NameAndStatus(role.toLowerCase(), "ACTIVE");
        return users.stream().map(user -> {
            EmployeeWorkResponse dto = new EmployeeWorkResponse();
            dto.setName(user.getUsername());
            dto.setRole(user.getRoleName().getName());
            return dto;
        }).collect(Collectors.toList());
    }

    public ShiftResponse addShift(ShiftRequest shiftRequest) {
        User user = userRepository.findByUsername(shiftRequest.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        LocalDate shiftDate = LocalDate.parse(shiftRequest.getDate());
        LocalDate today = LocalDate.now();

        if (shiftDate.isBefore(today)) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        List<WorkSchedule> existingSchedules = workScheduleRepository.findByDate(shiftDate);
        for (WorkSchedule schedule : existingSchedules) {
            if (schedule.getUser().getUsername().equals(shiftRequest.getName()) &&
                    schedule.getShiftType().equalsIgnoreCase(shiftRequest.getShift().toUpperCase())) {
                throw new AppException(ErrorCode.DUPLICATE_SHIFT);
            }
        }

        WorkSchedule schedule = new WorkSchedule();
        schedule.setUser(user);
        schedule.setWorkDate(LocalDate.parse(shiftRequest.getDate()));
        schedule.setShiftType(shiftRequest.getShift().toUpperCase());

        switch (shiftRequest.getShift().toLowerCase()) {
            case "morning":
                schedule.setStartTime(LocalTime.of(8, 30));
                schedule.setEndTime(LocalTime.of(12, 30));
                break;
            case "afternoon":
                schedule.setStartTime(LocalTime.of(12, 30));
                schedule.setEndTime(LocalTime.of(17, 30));
                break;
            case "night":
                schedule.setStartTime(LocalTime.of(17, 30));
                schedule.setEndTime(LocalTime.of(22, 30));
                break;
            default:
                throw new AppException(ErrorCode.INVALID_SHIFT_TYPE);
        }

        WorkSchedule savedSchedule = workScheduleRepository.save(schedule);
        return convertToShiftResponse(savedSchedule);
    }

    public void deleteShift(Integer id) {
        workScheduleRepository.deleteById(id);
    }

    private ShiftResponse convertToShiftResponse(WorkSchedule schedule) {
        ShiftResponse dto = new ShiftResponse();
        dto.setId(schedule.getId());
        dto.setName(schedule.getUser().getUsername());
        dto.setRole(schedule.getUser().getRoleName().getName().toLowerCase());
        dto.setDate(schedule.getWorkDate().toString());
        dto.setShift(schedule.getShiftType().toLowerCase());
        return dto;
    }
}