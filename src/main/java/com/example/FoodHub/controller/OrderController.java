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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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


}
