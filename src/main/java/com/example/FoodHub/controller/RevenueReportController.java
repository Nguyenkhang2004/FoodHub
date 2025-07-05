package com.example.FoodHub.controller;

import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.DishSalesResponse;
import com.example.FoodHub.dto.response.RevenueReportResponse;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.service.RevenueReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://127.0.0.1:5500")
@PreAuthorize("hasRole('ADMIN')")
public class RevenueReportController {
    private final RevenueReportsService revenueReportsService;

    @GetMapping
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getDashboardData(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) String specificDate) {
        if (!isValidPeriod(period)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if ("specific".equals(period) && specificDate == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        RevenueReportResponse data = revenueReportsService.getDashboardData(period, specificDate);
        ApiResponse<RevenueReportResponse> response = ApiResponse.<RevenueReportResponse>builder()
                .code(1000)
                .message(data.getRevenue() == 0L && data.getOrders() == 0
                        ? "Không có dữ liệu cho khoảng thời gian này"
                        : "Dashboard data retrieved successfully")
                .result(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dish-sales")
    public ResponseEntity<ApiResponse<DishSalesResponse>> getDishSales(
            @RequestParam String period,
            @RequestParam(required = false) String specificDate,
            @RequestParam(required = false) Integer categoryId) {
        if (!isValidPeriod(period)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if ("specific".equals(period) && specificDate == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        DishSalesResponse data = revenueReportsService.getDishSalesData(period, specificDate, categoryId);
        return ResponseEntity.ok(ApiResponse.<DishSalesResponse>builder()
                .code(1000)
                .message(data.getDishNames().isEmpty() ? "Không có dữ liệu cho món ăn này" : "Dish sales retrieved successfully")
                .result(data)
                .build());
    }

    private boolean isValidPeriod(String period) {
        return period.equals("today") || period.equals("week") || period.equals("month") ||
                period.equals("year") || period.equals("specific");
    }
}