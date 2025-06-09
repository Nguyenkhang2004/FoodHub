package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.RestaurantTableRequest;
import com.example.FoodHub.dto.response.RestaurantTableResponse;
import com.example.FoodHub.entity.RestaurantTable;
import com.example.FoodHub.enums.TableStatus;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.RestaurantTableMapper;
import com.example.FoodHub.repository.RestaurantTableRepository;
import com.example.FoodHub.specification.TableSpecifications;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RestaurantTableService {
    RestaurantTableRepository tableRepository;
    RestaurantTableMapper tableMapper;

    public List<RestaurantTableResponse> getAllTables(String tableNumber, String status) {
        log.info("Fetching all restaurant tables");
        log.info("Fetching filtered restaurant tables");
        return tableRepository.findAll(TableSpecifications.filterTables(tableNumber, status)).stream()
                .map(tableMapper::toRestaurantTableResponse)
                .toList();
    }

    public List<RestaurantTableResponse> getTablesByArea(String area) {
        log.info("Fetching tables in area: {}", area);
        return tableRepository.findByArea(area).stream()
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

    public List<RestaurantTableResponse> getTablesByAreaAndStatus(String area, String status) {
        log.info("Fetching tables in area: {} with status: {}", area, status);
        return tableRepository.findByAreaAndStatus(area, status).stream()
                .map(tableMapper::toRestaurantTableResponse)
                .toList();
    }

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
}
