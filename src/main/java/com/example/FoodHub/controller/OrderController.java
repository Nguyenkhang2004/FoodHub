package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.RestaurantOrderRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.OrderItemResponse;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.service.RestaurantOrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

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
            @RequestParam(defaultValue = "10") int size,
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

    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> updateOrder(
            @PathVariable Integer orderId, @Valid @RequestBody RestaurantOrderRequest request) {
        RestaurantOrderResponse updatedOrder = orderService.updateOrder(orderId, request);
        ApiResponse<RestaurantOrderResponse> response = ApiResponse.<RestaurantOrderResponse>builder()
                .result(updatedOrder)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/status/{orderId}")
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> updateOrderStatus(
            @PathVariable Integer orderId, @RequestParam String status) {
        RestaurantOrderResponse updatedOrder = orderService.updateOrderStatus(orderId, status);
        ApiResponse<RestaurantOrderResponse> response = ApiResponse.<RestaurantOrderResponse>builder()
                .result(updatedOrder)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/items/status/{orderItemId}")
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> updateOrderItemStatus(
            @PathVariable Integer orderItemId, @RequestParam String status, @RequestParam(required = false) String note) {
        RestaurantOrderResponse updatedOrderItemStatus = orderService.updateOrderItemStatus(orderItemId, status);
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

    @GetMapping("/work-shift-orders")
    public ResponseEntity<ApiResponse<Page<RestaurantOrderResponse>>> getCurrentOrders(
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tableNumber,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam String startTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "ASC") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort), orderBy));
        Page<RestaurantOrderResponse> orderResponses = orderService.getMyWorkShiftOrders(area, status, tableNumber, minPrice, maxPrice, startTime, pageable);
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

//    @GetMapping("/area/{area}/current")
//    public ResponseEntity<ApiResponse<Page<RestaurantOrderResponse>>> getCurrentOrders(
//            @PathVariable String area,
//            @RequestParam(required = false) String status,
//            @RequestParam(required = false) Integer tableId,
//            @RequestParam(required = false) BigDecimal minPrice,
//            @RequestParam(required = false) BigDecimal maxPrice,
//            @RequestParam(required = false) Instant startTime,
//            @RequestParam(required = false) Instant endTime,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "createdAt") String SorderBy,
//            @RequestParam(defaultValue = "ASC") String sort
//    ) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort), SorderBy));
//        Page<RestaurantOrderResponse> orderResponses = orderService.getCurrentWorkShiftOrders(area, status, tableId, minPrice, maxPrice, startTime, pageable);
//        ApiResponse<Page<RestaurantOrderResponse>> response = ApiResponse.<Page<RestaurantOrderResponse>>builder()
//                .result(orderResponses)
//                .build();
//        return ResponseEntity.ok().body(response);
//    }
//@GetMapping("/completed")
//public ResponseEntity<ApiResponse<Page<RestaurantOrderResponse>>> getCompletedOrders(
//        @RequestParam(required = false) String period,
//        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
//        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
//        @RequestParam(required = false) String search,
//        @RequestParam(defaultValue = "0") int page,
//        @RequestParam(defaultValue = "10") int size,
//        @RequestParam(defaultValue = "createdAt") String orderBy,
//        @RequestParam(defaultValue = "ASC") String sort) {
//    Sort.Direction direction = Sort.Direction.fromString(sort);
//    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, orderBy));
//    Page<RestaurantOrderResponse> orders = orderService.getCompletedOrders(period, startDate, endDate, search, pageable);
//    ApiResponse<Page<RestaurantOrderResponse>> response = ApiResponse.<Page<RestaurantOrderResponse>>builder()
//            .result(orders)
//            .build();
//    return ResponseEntity.ok().body(response);
//}

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
    @PostMapping("/{orderId}/items")
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> addItemsToOrder(
            @PathVariable Integer orderId, @Valid @RequestBody RestaurantOrderRequest request) {
        RestaurantOrderResponse updatedOrder = orderService.addItemsToOrder(orderId, request);
        ApiResponse<RestaurantOrderResponse> response = ApiResponse.<RestaurantOrderResponse>builder()
                .result(updatedOrder)
                .build();
        return ResponseEntity.ok().body(response);
    }

}
