package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.PaymentResponse;
import com.example.FoodHub.dto.response.RevenueStatsResponseForCashier;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.service.CashierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/cashier")
public class CashierController {

    @Autowired
    private CashierService cashierService;

    // Thanh toán đơn hàng
    @PostMapping("/payment")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = cashierService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    // Hoàn/Hủy đơn hàng
    @PostMapping("/cancel-or-refund/{orderId}")
    public ResponseEntity<String> cancelOrRefundOrder(@PathVariable Integer orderId) {
        cashierService.cancelOrRefundOrder(orderId);
        return ResponseEntity.ok("Order cancelled or refunded successfully");
    }

    // Quản lý giao dịch theo ngày
    @GetMapping("/transactions")
    public ResponseEntity<List<PaymentResponse>> getTransactionsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        if (end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        List<PaymentResponse> transactions = cashierService.getTransactionsByDate(start, end);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date) {
        BigDecimal totalRevenue = cashierService.getTotalRevenueByDate(date);
        return ResponseEntity.ok(totalRevenue);
    }

    @GetMapping("/revenue-stats")
    public ResponseEntity<RevenueStatsResponseForCashier> getRevenueStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        if (date != null) {
            return ResponseEntity.ok(cashierService.getRevenueStatsByDate(date));
        } else if (start != null && end != null) {
            if (end.isBefore(start)) {
                throw new AppException(ErrorCode.INVALID_DATE_RANGE);
            }
            return ResponseEntity.ok(cashierService.getRevenueStatsByDateRange(start, end));
        } else {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
    }
}