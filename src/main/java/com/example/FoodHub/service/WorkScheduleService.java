package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.ShiftRequest;
import com.example.FoodHub.dto.request.WorkShiftLogRequest;
import com.example.FoodHub.dto.response.EmployeeWorkResponse;
import com.example.FoodHub.dto.response.ShiftResponse;
import com.example.FoodHub.dto.response.WorkShiftLogResponse;
import com.example.FoodHub.entity.User;
import com.example.FoodHub.entity.WorkSchedule;
import com.example.FoodHub.entity.WorkShiftLog;
import com.example.FoodHub.enums.ShiftStatus;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.WorkScheduleMapper;
import com.example.FoodHub.mapper.WorkShiftLogMapper;
import com.example.FoodHub.repository.UserRepository;
import com.example.FoodHub.repository.WorkScheduleRepository;
import com.example.FoodHub.repository.WorkShiftLogRepository;
import com.example.FoodHub.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.FoodHub.enums.ShiftStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkScheduleService {
    WorkScheduleRepository workScheduleRepository;
    UserRepository userRepository;
    WorkScheduleMapper workScheduleMapper;
    WorkShiftLogMapper workShiftLogMapper;
    WorkShiftLogRepository workShiftLogRepository;

    public List<ShiftResponse> getShiftsForWeek(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<WorkSchedule> schedules = workScheduleRepository.findByWeek(weekStart, weekEnd);
        return schedules.stream()
                .map(schedule -> workScheduleMapper.toShiftResponse(schedule, schedule.getWorkDate()))
                .collect(Collectors.toList());
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
        return workScheduleMapper.toShiftResponse(savedSchedule, savedSchedule.getWorkDate());
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
        dto.setDate(schedule.getWorkDate().atStartOfDay(ZoneId.of("UTC")).toInstant());
        dto.setShift(schedule.getShiftType().toLowerCase());
        dto.setArea(schedule.getArea());
        dto.setStartTime(schedule.getStartTime().atDate(schedule.getWorkDate()).atZone(ZoneId.of("UTC")).toInstant());
        dto.setEndTime(schedule.getEndTime().atDate(schedule.getWorkDate()).atZone(ZoneId.of("UTC")).toInstant());
        return dto;
    }

    @PreAuthorize("hasAuthority('VIEW_WORK_SCHEDULE')")
    public ShiftResponse getMyWorkScheduleToday() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        WorkSchedule schedule = workScheduleRepository.findCurrentWorkShift(user.getId(), LocalDate.now(), LocalTime.now())
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND));

        return workScheduleMapper.toShiftResponse(schedule, schedule.getWorkDate());
    }

    @PreAuthorize("hasAuthority('VIEW_WORK_SCHEDULE')")
    public List<ShiftResponse> getMyWorkSchedule() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        List<WorkSchedule> schedules = workScheduleRepository.findByUserId(user.getId());
        if (schedules.isEmpty()) {
            throw new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND);
        }

        return schedules.stream()
                .map(schedule -> workScheduleMapper.toShiftResponse(schedule, schedule.getWorkDate()))
                .collect(Collectors.toList());
    }

    public WorkShiftLogResponse checkIn(WorkShiftLogRequest request) {
        log.info("Processing check-in request: {}", request);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer userId;

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            userId = ((Number) jwtAuth.getToken().getClaim("id")).intValue();
        } else {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        log.info("User ID from JWT: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        WorkSchedule schedule = workScheduleRepository
                .findById(request.getShiftId())
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND));

        Instant checkInTime = TimeUtils.getNowInVietNam();
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");

        Instant scheduledStart = ZonedDateTime.of(schedule.getWorkDate(), schedule.getStartTime(), zone).toInstant();
        Instant scheduledEnd = ZonedDateTime.of(schedule.getWorkDate(), schedule.getEndTime(), zone).toInstant();
        Instant earliestAllowed = scheduledStart.minus(Duration.ofMinutes(30));

        if (checkInTime.isBefore(earliestAllowed)) {
            throw new AppException(ErrorCode.CHECKIN_TOO_EARLY);
        }

        if (checkInTime.isAfter(scheduledEnd)) {
            throw new AppException(ErrorCode.CHECKIN_TOO_LATE);
        }

        String status = determineStatusCheckIn(checkInTime, schedule);

        // Ghi log
        WorkShiftLog workShiftLog = WorkShiftLog.builder()
                .user(user)
                .workSchedule(schedule)
                .checkInTime(checkInTime)
                .status(status)
                .build();

        return workShiftLogMapper.toWorkShiftLogResponse(workShiftLogRepository.save(workShiftLog));
    }


    public WorkShiftLogResponse checkOut(WorkShiftLogRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer userId;

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            userId = ((Number) jwtAuth.getToken().getClaim("id")).intValue();
        } else {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        WorkSchedule schedule = workScheduleRepository
                .findById(request.getShiftId())
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND));

        Instant checkOutTime = TimeUtils.getNowInVietNam();

        // Cập nhật log hiện tại
        WorkShiftLog workShiftLog = workShiftLogRepository.findByUserAndWorkSchedule(user, schedule)
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SHIFT_LOG_NOT_FOUND));

        workShiftLog.setCheckOutTime(checkOutTime);
        String status = determineFinalStatus(workShiftLog.getStatus(), checkOutTime, schedule);
        workShiftLog.setStatus(status);

        return workShiftLogMapper.toWorkShiftLogResponse(workShiftLogRepository.save(workShiftLog));
    }

    private String determineStatusCheckIn(Instant actualTime, WorkSchedule schedule) {
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");

        ZonedDateTime scheduledStart = ZonedDateTime.of(schedule.getWorkDate(), schedule.getStartTime(), zone);
        return actualTime.isAfter(scheduledStart.toInstant()) ? LATE.name() : ON_TIME.name();
    }

    public String determineFinalStatus(String currentStatus, Instant checkOutTime, WorkSchedule schedule) {
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        Instant scheduledEnd = ZonedDateTime.of(schedule.getWorkDate(), schedule.getEndTime(), zone).toInstant();

        boolean leftEarly = checkOutTime.isBefore(scheduledEnd);

        if (LATE.name().equals(currentStatus)) {
            return leftEarly ? LATE_AND_LEFT_EARLY.name() : LATE.name();
        } else if (ON_TIME.name().equals(currentStatus)) {
            return leftEarly ? LEFT_EARLY.name() : ON_TIME.name();
        } else {
            return currentStatus; // fallback
        }
    }

}