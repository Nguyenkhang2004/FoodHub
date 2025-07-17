package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.PayOSRequest;
import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.*;

import com.example.FoodHub.service.EmailService;
import com.example.FoodHub.service.PaymentService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    PaymentService paymentService;
    EmailService emailService;



    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
                .result(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPayments(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) BigDecimal minPrice,    // THÊM
            @RequestParam(required = false) BigDecimal maxPrice,    // THÊM
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "ASC") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort), orderBy));

        Page<PaymentResponse> payments = paymentService.getPayments(
                period, startDate, endDate, status, transactionId,
                paymentMethod, minPrice, maxPrice, // THÊM 2 PARAMETER
                pageable
        );

        return ResponseEntity.ok(ApiResponse.<Page<PaymentResponse>>builder().result(payments).build());
    }
    @PreAuthorize("hasRole('ADMIN')")
    // Xem chi tiết giao dịch (hóa đơn liên quan) (endpoint mới)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> getPaymentDetails(
            @PathVariable Integer id) {
        RestaurantOrderResponse orderDetails = paymentService.getPaymentDetails(id);
        return ResponseEntity.ok(ApiResponse.<RestaurantOrderResponse>builder().result(orderDetails).build());
    }


    @PutMapping("/callback")
    public ResponseEntity<ApiResponse<PaymentResponse>> paymentCallback(@RequestBody PayOSRequest request) {
        PaymentResponse response = paymentService.updatePayOSPaymentStatus(request);

        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
                .result(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }



//trên là của khang
//==================================//==================================//==================================
//==================================//==================================//==================================


    @GetMapping("/check-new-orders")
    public ApiResponse<List<PaymentResponse>> checkNewOrders() {
        log.info("Checking new orders at: {} (UTC+7)", Instant.now().atZone(ZoneId.of("Asia/Ho_Chi_Minh")));
            List<PaymentResponse> newOrders = paymentService.getNewOrders();
            return ApiResponse.<List<PaymentResponse>>builder()
                    .code(1000)
                    .message("Success")
                    .result(newOrders)
                    .build();
    }


    // Thanh toán đơn hàng
    @PostMapping("/payment2")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment2(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
                .result(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }



// Hủy/Hoàn tiền đơn hàng

    @PostMapping("/cancel-or-refund/{orderId}")
    public ResponseEntity<ApiResponse<String>> cancelOrRefundOrder(@PathVariable Integer orderId) {
        paymentService.cancelOrRefundOrder(orderId);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .result("Đơn hàng đã được hủy thành công")
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }


    // Lấy giao dịch hôm nay
    @GetMapping("/todays-transactions")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getTodaysTransactions() {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            Instant start = now.toLocalDate().atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
            Instant end = now.toLocalDate().plusDays(1).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).minusSeconds(1).toInstant();
            List<PaymentResponse> transactions = paymentService.getTransactionsByDate(start, end);
            ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder()
                    .code(0)
                    .result(transactions)
                    .build();
            return ResponseEntity.ok().body(apiResponse);
        }


    // Lấy thống kê doanh thu hôm nay
    @GetMapping("/todays-revenue-stats")
    public ResponseEntity<ApiResponse<RevenueStatsResponseForCashier>> getTodaysRevenueStats() {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            Instant date = now.toLocalDate().atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
            RevenueStatsResponseForCashier stats = paymentService.getRevenueStatsByDate(date);
            ApiResponse<RevenueStatsResponseForCashier> apiResponse = ApiResponse.<RevenueStatsResponseForCashier>builder()
                    .code(0)
                    .result(stats)
                    .build();
            return ResponseEntity.ok().body(apiResponse);
        }



    // Lấy thông tin hóa đơn
    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable Integer orderId) {
        InvoiceResponse response = paymentService.getOrderDetails(orderId);
        ApiResponse<InvoiceResponse> apiResponse = ApiResponse.<InvoiceResponse>builder()
                .result(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    // Gửi email hóa đơn
    @PostMapping("/send-invoice-email")
    public ResponseEntity<ApiResponse<String>> sendInvoiceEmail(@RequestBody Map<String, String> request) {
        String customerEmail = request.get("customerEmail");
        Integer orderId = Integer.parseInt(request.get("orderId"));
        InvoiceResponse invoiceResponse = paymentService.getOrderDetails(orderId);
        emailService.sendInvoiceEmailAsync(customerEmail, invoiceResponse);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .result("Email invoice sent successfully")
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }



    @GetMapping("/invoice/{orderId}/pdf")
    public ResponseEntity<String> generateInvoicePdf(@PathVariable Integer orderId) {
        return ResponseEntity.ok(paymentService.generateInvoicePdf(orderId));
    }



    @GetMapping("/search-transactions")
    public ResponseEntity<ApiResponse<?>> searchTransactions(@RequestParam String query) {
        ApiResponse<?> response = paymentService.searchTransactions(query);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<?>> getSuggestions(@RequestParam String query) {
        ApiResponse<?> response = paymentService.getSuggestions(query);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(@PathVariable Integer orderId) {
        log.info("Fetching payment status for order ID: {}", orderId);
        PaymentResponse response = paymentService.getPaymentStatus(orderId);
        ApiResponse<PaymentResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return ResponseEntity.ok(apiResponse);
    }



}