package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.RestaurantTableRequest;
import com.example.FoodHub.dto.response.RestaurantTableResponse;
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
import org.springframework.data.domain.Sort;
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


    public RestaurantTableResponse updateTableStatus(Integer tableId, String status) {
        log.info("Updating table status for table ID: {} to {}", tableId, status);
        var table = tableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
        if (status == null || !Arrays.stream(TableStatus.values())
                .anyMatch(tableStatus -> tableStatus.name().equalsIgnoreCase(status))) {
            throw new AppException(ErrorCode.INVALID_TABLE_STATUS);
        }
        table.setStatus(status);
        tableRepository.save(table);
        return tableMapper.toRestaurantTableResponse(table);
    }

    public RestaurantTableResponse createTable(RestaurantTableRequest request) {
        var entity = tableMapper.toRestaurantTable(request);
        entity.setStatus(TableStatus.AVAILABLE.name());
        tableRepository.save(entity);
        return tableMapper.toRestaurantTableResponse(entity);
    }

    public RestaurantTableResponse deleteTable(Integer tableId) {
        var table = tableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));

        if (TableStatus.OCCUPIED.name().equalsIgnoreCase(table.getStatus())) {
            throw new AppException(ErrorCode.TABLE_OCCUPIED_CANNOT_DELETE);
        }

        table.setStatus(TableStatus.UNAVAILABLE.name());
        tableRepository.save(table);
        return tableMapper.toRestaurantTableResponse(table);
    }

    public RestaurantTableResponse restoreTable(Integer tableId) {
        var table = tableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));

        table.setStatus(TableStatus.AVAILABLE.name());
        tableRepository.save(table);
        return tableMapper.toRestaurantTableResponse(table);
    }

    public RestaurantTableResponse getTableById(Integer tableId) {
        var table = tableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
        return tableMapper.toRestaurantTableResponse(table);
    }



}
