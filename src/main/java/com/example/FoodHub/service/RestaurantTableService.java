package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.RestaurantTableRequest;
import com.example.FoodHub.dto.response.RestaurantTableResponse;
import com.example.FoodHub.entity.WorkSchedule;
import com.example.FoodHub.entity.WorkShiftLog;
import com.example.FoodHub.enums.TableStatus;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.RestaurantTableMapper;
import com.example.FoodHub.repository.RestaurantTableRepository;
import com.example.FoodHub.repository.WorkScheduleRepository;
import com.example.FoodHub.repository.WorkShiftLogRepository;
import com.example.FoodHub.specification.TableSpecifications;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RestaurantTableService {
    RestaurantTableRepository tableRepository;
    RestaurantTableMapper tableMapper;
    WorkShiftLogRepository workShiftLogRepository;
    WorkScheduleRepository workScheduleRepository;

    public List<RestaurantTableResponse> getAllTables(
            String tableNumber, String status, String area, String orderBy, String sort) {
        log.info("Fetching all restaurant tables");
        log.info("Fetching filtered restaurant tables");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sort), orderBy);
        return tableRepository.findAll(TableSpecifications.filterTables(tableNumber, status, area), sortOrder).stream()
                .map(tableMapper::toRestaurantTableResponse)
                .toList();
    }

    public List<RestaurantTableResponse> getTablesByStatus(String status) {
        log.info("Fetching tables with status: {}", status);
        return tableRepository.findByStatus(status).stream()
                .map(tableMapper::toRestaurantTableResponse)
                .toList();
    }

    public RestaurantTableResponse updateTable(Integer tableId, RestaurantTableRequest request) {
        log.info("Updating table with ID: {}", tableId);
        if (request == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        var table = tableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
        tableMapper.updateTable(table, request);
        tableRepository.save(table);
        return tableMapper.toRestaurantTableResponse(table);
    }

    @PreAuthorize("hasAuthority('ASSIGN_TABLE')")
    @Transactional
    public RestaurantTableResponse updateTableStatus (Integer tableId, String status) {
        log.info("Updating table status for table ID: {} to {}", tableId, status);
        var table = tableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
        if(status == null || !Arrays.stream(TableStatus.values())
                .anyMatch(tableStatus -> tableStatus.name().equalsIgnoreCase(status))) {
            throw new AppException(ErrorCode.INVALID_TABLE_STATUS);
        }
        table.setStatus(status);
        tableRepository.save(table);
        return tableMapper.toRestaurantTableResponse(table);
    }

    public List<RestaurantTableResponse> getTablesByWaiterId(Integer waiterId) {
        log.info("Fetching tables assigned to waiter with ID: {}", waiterId);
        WorkSchedule currentSchedule = workScheduleRepository.findCurrentWorkShift(waiterId, LocalDate.now(), LocalTime.now())
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND));
        WorkShiftLog currentShiftLog = workShiftLogRepository.findByWorkScheduleId(currentSchedule.getId())
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SHIFT_LOG_NOT_FOUND));
        if(currentShiftLog.getCheckInTime() == null) {
            throw new AppException(ErrorCode.USER_NOT_CHECKED_IN);
        }
        return tableRepository.findByArea(currentSchedule.getArea()).stream()
                .map(tableMapper::toRestaurantTableResponse)
                .toList();
    }
}
