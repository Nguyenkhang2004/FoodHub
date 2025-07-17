package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.RestaurantOrderRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.NotificationResponse;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.service.NotificationService;
import com.example.FoodHub.service.RestaurantOrderService;
import jakarta.validation.Valid;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    RestaurantOrderService orderService;
    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> createOrder(@Valid @RequestBody RestaurantOrderRequest request) {
        RestaurantOrderResponse orderResponse = orderService.createOrder(request);
        ApiResponse<RestaurantOrderResponse> response = ApiResponse.<RestaurantOrderResponse>builder()
                .result(orderResponse)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RestaurantOrderResponse>>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tableNumber,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "ASC") String sort
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, orderBy));
        Page<RestaurantOrderResponse> orderResponses = orderService.getAllOrders(status, tableNumber, minPrice, maxPrice, pageable);
        ApiResponse<Page<RestaurantOrderResponse>> response = ApiResponse.<Page<RestaurantOrderResponse>>builder()
                .result(orderResponses)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/status/{orderId}")
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> updateOrderStatus(
            @PathVariable Integer orderId, @RequestParam String status, @RequestParam(required = false) String note) {
        RestaurantOrderResponse updatedOrder = orderService.updateOrderStatus(orderId, status, note);
        ApiResponse<RestaurantOrderResponse> response = ApiResponse.<RestaurantOrderResponse>builder()
                .result(updatedOrder)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/items/status/{orderItemId}")
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> updateOrderItemStatus(
            @PathVariable Integer orderItemId, @RequestParam String status, @RequestParam(required = false) String note) {
        RestaurantOrderResponse updatedOrderItemStatus = orderService.updateOrderItemStatus(orderItemId, status, note);
        ApiResponse<RestaurantOrderResponse> response = ApiResponse.<RestaurantOrderResponse>builder()
                .result(updatedOrderItemStatus)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/table/{tableId}/current")
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> getOrdersByTableId(
            @PathVariable Integer tableId
    ) {
        RestaurantOrderResponse orderResponses = orderService.getCurrentOrdersByTableId(tableId);
        ApiResponse<RestaurantOrderResponse> response = ApiResponse.<RestaurantOrderResponse>builder()
                .result(orderResponses)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/waiter/work-shift-orders/{userId}")
    public ResponseEntity<ApiResponse<Page<RestaurantOrderResponse>>> getWaiterWorkShiftOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tableNumber,
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "ASC") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort), orderBy));
        Page<RestaurantOrderResponse> orderResponses = orderService.getWaiterWorkShiftOrders(status, tableNumber, userId, pageable);
        ApiResponse<Page<RestaurantOrderResponse>> response = ApiResponse.<Page<RestaurantOrderResponse>>builder()
                .result(orderResponses)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/chef/work-shift-orders/{userId}")
    public ResponseEntity<ApiResponse<Page<RestaurantOrderResponse>>> getChefWorkShiftOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tableNumber,
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "ASC") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort), orderBy));
        Page<RestaurantOrderResponse> orderResponses = orderService.getChefWorkShiftOrders(status, tableNumber, userId, pageable);
        ApiResponse<Page<RestaurantOrderResponse>> response = ApiResponse.<Page<RestaurantOrderResponse>>builder()
                .result(orderResponses)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> getOrderById(@PathVariable Integer orderId) {
        RestaurantOrderResponse orderResponse = orderService.getOrdersByOrderId(orderId);
        ApiResponse<RestaurantOrderResponse> response = ApiResponse.<RestaurantOrderResponse>builder()
                .result(orderResponse)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> addItemsToOrder(
            @PathVariable Integer orderId, @Valid @RequestBody RestaurantOrderRequest request) {
        RestaurantOrderResponse updatedOrder = orderService.addItemsToOrder(orderId, request);
        ApiResponse<RestaurantOrderResponse> response = ApiResponse.<RestaurantOrderResponse>builder()
                .result(updatedOrder)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/table/{tableId}/current/customer")
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> getCurrentOrderForCustomer(
            @PathVariable Integer tableId,
            @RequestHeader("Authorization") String bearerToken) {

        String token = bearerToken.replace("Bearer", "").trim();

        RestaurantOrderResponse response = orderService.getOrderForCustomerByToken(tableId, token);

        return ResponseEntity.ok(ApiResponse.<RestaurantOrderResponse>builder()
                .result(response)
                .build());
    }

    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<Page<RestaurantOrderResponse>>> getCompletedOrders(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "ASC") String sort) {
        Sort.Direction direction = Sort.Direction.fromString(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, orderBy));
        Page<RestaurantOrderResponse> orders = orderService.getCompletedOrders(period, startDate, endDate, search, pageable);
        ApiResponse<Page<RestaurantOrderResponse>> response = ApiResponse.<Page<RestaurantOrderResponse>>builder()
                .result(orders)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderSummary(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        Map<String, Object> summary = orderService.getOrderSummary(period, startDate, endDate);
        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .result(summary)
                .build();
        return ResponseEntity.ok().body(response);
    }

    // Endpoint mới
    @GetMapping("/filtered")
    public ResponseEntity<ApiResponse<Page<RestaurantOrderResponse>>> getOrdersFiltered(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "ASC") String sort) {

        Sort.Direction direction = Sort.Direction.fromString(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, orderBy));

        Page<RestaurantOrderResponse> orders = orderService.getOrdersFiltered(
                status, period, startDate, endDate, orderType, minPrice, maxPrice, paymentMethod, search, pageable);

        ApiResponse<Page<RestaurantOrderResponse>> response = ApiResponse.<Page<RestaurantOrderResponse>>builder()
                .result(orders)
                .build();
        return ResponseEntity.ok().body(response);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<RestaurantOrderResponse>>> getOrdersByUserId(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "DESC") String sort
    ) {
        log.info("Fetching orders for user ID: {}", userId);
        Sort.Direction direction = Sort.Direction.fromString(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, orderBy));
        Page< RestaurantOrderResponse> orderResponses = orderService.getAllOrdersByUserId(userId, pageable);
        ApiResponse<Page<RestaurantOrderResponse>> response = ApiResponse.<Page<RestaurantOrderResponse>>builder()
                .result(orderResponses)
                .build();
        return ResponseEntity.ok().body(response);
    }
}
