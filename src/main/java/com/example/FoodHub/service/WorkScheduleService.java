package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.ShiftRequest;
import com.example.FoodHub.dto.response.ShiftResponse;
import com.example.FoodHub.dto.request.EmployeeWorkRequest;
import com.example.FoodHub.dto.response.EmployeeWorkResponse;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.entity.WorkSchedule;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.WorkScheduleMapper;
import com.example.FoodHub.repository.UserRepository;
import com.example.FoodHub.repository.WorkScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkScheduleService {
    WorkScheduleRepository workScheduleRepository;

    UserRepository userRepository;

    private final WorkScheduleMapper workScheduleMapper;

    public List<ShiftResponse> getShiftsForWeek(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<WorkSchedule> schedules = workScheduleRepository.findByWeek(weekStart, weekEnd);
        return schedules.stream().map(workScheduleMapper::toShiftResponse).collect(Collectors.toList());
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
        LocalTime currentTime = LocalTime.now();

        // Kiểm tra ngày trong quá khứ
        if (shiftDate.isBefore(today)) {
            throw new AppException(ErrorCode.PAST_DATE_NOT_ALLOWED);
        }

        // Kiểm tra ca hôm nay đã qua giờ bắt đầu
        if (shiftDate.isEqual(today)) {
            switch (shiftRequest.getShift().toLowerCase()) {
                case "morning":
                    if (currentTime.isAfter(LocalTime.of(8, 30))) {
                        throw new AppException(ErrorCode.PAST_SHIFT_TIME);
                    }
                    break;
                case "afternoon":
                    if (currentTime.isAfter(LocalTime.of(12, 30))) {
                        throw new AppException(ErrorCode.PAST_SHIFT_TIME);
                    }
                    break;
                case "night":
                    if (currentTime.isAfter(LocalTime.of(17, 30))) {
                        throw new AppException(ErrorCode.PAST_SHIFT_TIME);
                    }
                    break;
                default:
                    throw new AppException(ErrorCode.INVALID_SHIFT_TYPE);
            }
        }

        // Kiểm tra ca trùng lặp
        List<WorkSchedule> existingSchedules = workScheduleRepository.findByDate(shiftDate);
        for (WorkSchedule schedule : existingSchedules) {
            if (schedule.getUser().getUsername().equals(shiftRequest.getName()) &&
                    schedule.getShiftType().equalsIgnoreCase(shiftRequest.getShift().toUpperCase())) {
                throw new AppException(ErrorCode.DUPLICATE_SHIFT);
            }
        }

        // Sử dụng mapper để ánh xạ ShiftRequest sang WorkSchedule
        WorkSchedule schedule = workScheduleMapper.toWorkSchedule(shiftRequest);
        schedule.setUser(user); // Gán user thủ công

        // Gán thời gian bắt đầu và kết thúc theo loại ca
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
        return workScheduleMapper.toShiftResponse(savedSchedule);
    }

    public void deleteShift(Integer id) {
        WorkSchedule schedule = workScheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));
        LocalDate shiftDate = schedule.getWorkDate();
        LocalDate today = LocalDate.now();

        // Kiểm tra ngày trong quá khứ
        if (shiftDate.isBefore(today)) {
            throw new AppException(ErrorCode.PAST_DATE_NOT_ALLOWED);
        }

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

    public ShiftResponse getMyWorkScheduleToday(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<WorkSchedule> schedules = workScheduleRepository.findByUserIdAndDateFromToday(user.getId(), LocalDate.now());
        if (schedules.isEmpty()) {
            throw new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND);
        }

        return workScheduleMapper.toShiftResponse(schedules.get(0));
    }

    public List<ShiftResponse> getMyWorkSchedule(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        List<WorkSchedule> schedules = workScheduleRepository.findByUserId(user.getId());
        if (schedules.isEmpty()) {
            throw new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND);
        }

        return schedules.stream()
                .map(workScheduleMapper::toShiftResponse)
                .collect(Collectors.toList());
    }
}