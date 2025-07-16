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
import java.util.Comparator;
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

        // Kiểm tra ca hôm nay đã qua giờ bắt đầu (chỉ áp dụng cho ca không phải FULL_DAY)
        if (shiftDate.isEqual(today) && !shiftRequest.getShift().equalsIgnoreCase("FULL_DAY")) {
            switch (shiftRequest.getShift().toUpperCase()) {
                case "MORNING":
                    if (currentTime.isAfter(LocalTime.of(8, 30))) {
                        throw new AppException(ErrorCode.PAST_SHIFT_TIME);
                    }
                    break;
                case "AFTERNOON":
                    if (currentTime.isAfter(LocalTime.of(12, 30))) {
                        throw new AppException(ErrorCode.PAST_SHIFT_TIME);
                    }
                    break;
                case "EVENING":
                    if (currentTime.isAfter(LocalTime.of(17, 30))) {
                        throw new AppException(ErrorCode.PAST_SHIFT_TIME);
                    }
                    break;
                default:
                    throw new AppException(ErrorCode.INVALID_SHIFT_TYPE);
            }
        }

        // Kiểm tra ca trùng lặp và logic FULL_DAY
        List<WorkSchedule> existingSchedules = workScheduleRepository.findByDate(shiftDate);

        // Kiểm tra nếu user đã có ca FULL_DAY trong ngày
        boolean userHasFullDay = existingSchedules.stream()
                .anyMatch(schedule -> schedule.getUser().getUsername().equals(shiftRequest.getName())
                        && schedule.getShiftType().equalsIgnoreCase("FULL_DAY"));

        if (userHasFullDay) {
            throw new AppException(ErrorCode.DUPLICATE_SHIFT); // hoặc tạo error code mới cho trường hợp này
        }

        // Kiểm tra nếu đang thêm ca FULL_DAY nhưng user đã có ca khác trong ngày
        if (shiftRequest.getShift().equalsIgnoreCase("FULL_DAY")) {
            boolean userHasOtherShift = existingSchedules.stream()
                    .anyMatch(schedule -> schedule.getUser().getUsername().equals(shiftRequest.getName()));

            if (userHasOtherShift) {
                throw new AppException(ErrorCode.DUPLICATE_SHIFT); // hoặc tạo error code mới
            }
        }

        // Kiểm tra ca trùng lặp thông thường
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
        switch (shiftRequest.getShift().toUpperCase()) {
            case "MORNING":
                schedule.setStartTime(LocalTime.of(8, 30));
                schedule.setEndTime(LocalTime.of(12, 30));
                break;
            case "AFTERNOON":
                schedule.setStartTime(LocalTime.of(12, 30));
                schedule.setEndTime(LocalTime.of(17, 30));
                break;
            case "EVENING":
                schedule.setStartTime(LocalTime.of(17, 30));
                schedule.setEndTime(LocalTime.of(22, 30));
                break;
            case "FULL_DAY":
                schedule.setStartTime(LocalTime.of(8, 30));
                schedule.setEndTime(LocalTime.of(22, 30));
                break;
            default:
                throw new AppException(ErrorCode.INVALID_SHIFT_TYPE);
        }

        WorkSchedule savedSchedule = workScheduleRepository.save(schedule);
        WorkShiftLog workShiftLog = WorkShiftLog.builder()
                .user(user)
                .workSchedule(savedSchedule)
                .checkInTime(null) // Chưa check-in
                .checkOutTime(null) // Chưa check-out
                .status(ShiftStatus.UNSCHEDULED.name()) // Trạng thái ban đầu
                .build();
        workShiftLogRepository.save(workShiftLog);
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

    @PreAuthorize("hasAuthority('VIEW_WORK_SCHEDULE')")
    public ShiftResponse getMyWorkScheduleToday() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<WorkSchedule> schedules = workScheduleRepository.findAllTodaySchedules(user.getId(), LocalDate.now());
        if(schedules.isEmpty()) {
            throw new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND);
        }
        if (schedules.size() == 1) {
            return workScheduleMapper.toShiftResponse(schedules.get(0), schedules.get(0).getWorkDate());
        }
        WorkSchedule currentSchedule = null;
        for (WorkSchedule schedule : schedules) {
            currentSchedule = schedule;
            WorkShiftLog existingLog = workShiftLogRepository
                    .findByWorkScheduleId(schedule.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.WORK_SHIFT_LOG_NOT_FOUND));
            if(existingLog.getStatus().equals(ShiftStatus.ABSENT.name())) {
                continue; // Bỏ qua nếu đã đánh dấu vắng
            }

            if(existingLog.getStatus().equals(ShiftStatus.UNSCHEDULED.name())) {
                break;
            }

            if( existingLog.getCheckInTime() != null && existingLog.getCheckOutTime() == null) {
                break;
            }
        }
        return workScheduleMapper.toShiftResponse(currentSchedule, currentSchedule.getWorkDate());

    }

    @PreAuthorize("hasAuthority('VIEW_WORK_SCHEDULE')")
    public List<ShiftResponse> getMyWorkSchedule() {
        Integer userId;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            userId = ((Number) jwtAuth.getToken().getClaim("id")).intValue();
        } else {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        User user = userRepository.findById(userId)
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

        WorkSchedule schedule = workScheduleRepository
                .findById(request.getShiftId())
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND));

        WorkShiftLog existingLog = workShiftLogRepository
                .findByWorkScheduleId(schedule.getId())
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SHIFT_LOG_NOT_FOUND));
        if( existingLog != null && existingLog.getCheckInTime() != null) {
            throw new AppException(ErrorCode.WORK_SHIFT_LOG_ALREADY_CHECK_IN);
        }

        if( existingLog != null && existingLog.getCheckOutTime() != null) {
            throw new AppException(ErrorCode.WORK_SHIFT_LOG_ALREADY_CHECK_OUT);
        }

        Instant checkInTime = TimeUtils.getNowInVietNam(); // Đã là giờ VN

        Instant scheduledStart = schedule.getStartTime().atDate(schedule.getWorkDate()).toInstant(ZoneOffset.UTC);
        Instant scheduledEnd = schedule.getEndTime().atDate(schedule.getWorkDate()).toInstant(ZoneOffset.UTC);

        Instant earliestAllowed = scheduledStart.minus(Duration.ofMinutes(30));

        if (checkInTime.isBefore(earliestAllowed)) {
            throw new AppException(ErrorCode.CHECKIN_TOO_EARLY);
        }

        if (checkInTime.isAfter(scheduledEnd)) {
            throw new AppException(ErrorCode.CHECKIN_TOO_LATE);
        }

        String status = determineStatusCheckIn(checkInTime, schedule);

        existingLog.setCheckInTime(checkInTime);
        existingLog.setStatus(status);

        return workShiftLogMapper.toWorkShiftLogResponse(workShiftLogRepository.save(existingLog));
    }


    public WorkShiftLogResponse checkOut(WorkShiftLogRequest request) {
        WorkSchedule schedule = workScheduleRepository
                .findById(request.getShiftId())
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND));

        WorkShiftLog existingLog = workShiftLogRepository
                .findByWorkScheduleId(schedule.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_CHECKED_IN));

        if (existingLog.getCheckOutTime() != null) {
            throw new AppException(ErrorCode.WORK_SHIFT_LOG_ALREADY_CHECK_OUT);
        }

        Instant checkOutTime = TimeUtils.getNowInVietNam();
        existingLog.setCheckOutTime(checkOutTime);
        String status = determineFinalStatus(existingLog.getStatus(), checkOutTime, schedule);
        existingLog.setStatus(status);

        return workShiftLogMapper.toWorkShiftLogResponse(workShiftLogRepository.save(existingLog));
    }

    private String determineStatusCheckIn(Instant actualTime, WorkSchedule schedule) {
        Instant scheduledStart = schedule.getStartTime().atDate(schedule.getWorkDate()).toInstant(ZoneOffset.UTC);
        return actualTime.isAfter(scheduledStart) ? LATE.name() : ON_TIME.name();
    }

    public String determineFinalStatus(String currentStatus, Instant checkOutTime, WorkSchedule schedule) {
        Instant scheduledEnd = schedule.getEndTime().atDate(schedule.getWorkDate()).toInstant(ZoneOffset.UTC);

        boolean leftEarly = checkOutTime.isBefore(scheduledEnd);

        if (LATE.name().equals(currentStatus)) {
            return leftEarly ? LATE_AND_LEFT_EARLY.name() : LATE.name();
        } else if (ON_TIME.name().equals(currentStatus)) {
            return leftEarly ? LEFT_EARLY.name() : ON_TIME.name();
        } else {
            return currentStatus; // fallback
        }
    }

    public List<ShiftResponse> getMyWorkShiftToday() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer userId;

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            userId = ((Number) jwtAuth.getToken().getClaim("id")).intValue();
        } else {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        LocalDate today = LocalDate.now();

        List<WorkSchedule> schedules = workScheduleRepository
                .findByUserIdAndWorkDate(user.getId(), today);

        if (schedules.isEmpty()) {
            throw new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND);
        }

        return schedules.stream()
                .sorted(Comparator.comparing(WorkSchedule::getStartTime)) // Sắp xếp theo thời gian bắt đầu
                .map(schedule -> workScheduleMapper.toShiftResponse(schedule, schedule.getWorkDate()))
                .collect(Collectors.toList());
    }

    public WorkShiftLogResponse getWorkShiftLogByWorkScheduleId(Integer workScheduleId) {

        WorkShiftLog workShiftLog = workShiftLogRepository
                .findByWorkScheduleId(workScheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SHIFT_LOG_NOT_FOUND));

        return workShiftLogMapper.toWorkShiftLogResponse(workShiftLog);
    }

    public List<WorkShiftLogResponse> getMyWorkShiftLogs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer employeeId;
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            employeeId = ((Number) jwtAuth.getToken().getClaim("id")).intValue();
        } else {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        List<WorkShiftLog> logs = workShiftLogRepository.findByUserId(employeeId);
        if (logs.isEmpty()) {
            throw new AppException(ErrorCode.WORK_SHIFT_LOG_NOT_FOUND);
        }
        return logs.stream()
                .map(workShiftLogMapper::toWorkShiftLogResponse)
                .collect(Collectors.toList());
    }

}