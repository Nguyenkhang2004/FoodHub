package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.RestaurantTableRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.RestaurantTableResponse;
import com.example.FoodHub.service.RestaurantTableService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tables")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TableController {
    RestaurantTableService tableService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantTableResponse>>> getAllTables() {
        List<RestaurantTableResponse> tables = tableService.getAllTables();
        ApiResponse<List<RestaurantTableResponse>> response = ApiResponse.<List<RestaurantTableResponse>>builder()
                .result(tables)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<RestaurantTableResponse>>> getTablesByStatus(@PathVariable String status) {
        List<RestaurantTableResponse> tables = tableService.getTablesByStatus(status);
        ApiResponse<List<RestaurantTableResponse>> response = ApiResponse.<List<RestaurantTableResponse>>builder()
                .result(tables)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/area/{area}")
    public ResponseEntity<ApiResponse<List<RestaurantTableResponse>>> getTablesByArea(@PathVariable String area) {
        List<RestaurantTableResponse> tables = tableService.getTablesByArea(area);
        ApiResponse<List<RestaurantTableResponse>> response = ApiResponse.<List<RestaurantTableResponse>>builder()
                .result(tables)
                .build();
        return ResponseEntity.ok().body(response);
    }



    @PutMapping("/{tableId}")
    public ResponseEntity<ApiResponse<RestaurantTableResponse>> updateTableStatus(
            @PathVariable Integer tableId, @Valid @RequestBody RestaurantTableRequest request) {
        RestaurantTableResponse updatedTable = tableService.updateTable(tableId, request);
        ApiResponse<RestaurantTableResponse> response = ApiResponse.<RestaurantTableResponse>builder()
                .result(updatedTable)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/status/{tableId}")
    public ResponseEntity<ApiResponse<RestaurantTableResponse>> updateTableStatus(
            @PathVariable Integer tableId, @RequestParam String status) {
        RestaurantTableResponse updatedTable = tableService.updateTableStatus(tableId, status);
        ApiResponse<RestaurantTableResponse> response = ApiResponse.<RestaurantTableResponse>builder()
                .result(updatedTable)
                .build();
        return ResponseEntity.ok().body(response);
    }



}
