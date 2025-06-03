package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.PaymentResponse;
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date) {
        List<PaymentResponse> transactions = cashierService.getTransactionsByDate(date);
        return ResponseEntity.ok(transactions);
    }

    // Tổng doanh thu theo ngày
    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date) {
        BigDecimal totalRevenue = cashierService.getTotalRevenueByDate(date);
        return ResponseEntity.ok(totalRevenue);
    }
}