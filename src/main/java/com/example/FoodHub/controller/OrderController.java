package com.example.FoodHub.controller;

import com.example.FoodHub.dto.request.RestaurantOrderRequest;
import com.example.FoodHub.dto.response.ApiResponse;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.service.RestaurantOrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    RestaurantOrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> createOrder(@RequestBody RestaurantOrderRequest request) {
        RestaurantOrderResponse orderResponse = orderService.createOrder(request);
        ApiResponse response = ApiResponse.<RestaurantOrderResponse>builder()
                .result(orderResponse)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantOrderResponse>>> getAllOrders() {
        List<RestaurantOrderResponse> orderResponses = orderService.getAllOrders();
        ApiResponse<List<RestaurantOrderResponse>> response = ApiResponse.<List<RestaurantOrderResponse>>builder()
                .result(orderResponses)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> getOrdersByOrderId(@PathVariable Integer orderId) {
        RestaurantOrderResponse orderResponse = orderService.getOrdersByOrderId(orderId);
        ApiResponse<RestaurantOrderResponse> response = ApiResponse.<RestaurantOrderResponse>builder()
                .result(orderResponse)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/table/{tableNumber}")
    public ResponseEntity<ApiResponse<List<RestaurantOrderResponse>>> getOrdersByTableNumber(@PathVariable String tableNumber) {
        List<RestaurantOrderResponse> orderResponses = orderService.getOrderByTableNumber(tableNumber);
        ApiResponse<List<RestaurantOrderResponse>> response = ApiResponse.<List<RestaurantOrderResponse>>builder()
                .result(orderResponses)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<RestaurantOrderResponse>>> getOrdersByStatus(@PathVariable String status) {
        List<RestaurantOrderResponse> orderResponses = orderService.getOrdersByStatus(status);
        ApiResponse<List<RestaurantOrderResponse>> response = ApiResponse.<List<RestaurantOrderResponse>>builder()
                .result(orderResponses)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/table/{tableNumber}/status/{status}")
    public ResponseEntity<ApiResponse<List<RestaurantOrderResponse>>> getOrdersByTableNumberAndStatus(@PathVariable String tableNumber, @PathVariable String status) {
        List<RestaurantOrderResponse> orderResponses = orderService.getOrdersByTableNumberAndStatus(tableNumber, status);
        ApiResponse<List<RestaurantOrderResponse>> response = ApiResponse.<List<RestaurantOrderResponse>>builder()
                .result(orderResponses)
                .build();
        return ResponseEntity.ok().body(response);
    }


    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<RestaurantOrderResponse>> updateOrder(@PathVariable Integer orderId, @RequestBody RestaurantOrderRequest request) {
        RestaurantOrderResponse updatedOrder = orderService.updateOrder(orderId, request);
        ApiResponse<RestaurantOrderResponse> response = ApiResponse.<RestaurantOrderResponse>builder()
                .result(updatedOrder)
                .build();
        return ResponseEntity.ok().body(response);
    }

}
