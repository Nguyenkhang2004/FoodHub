package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.RestaurantOrderRequest;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.entity.RestaurantOrder;
import com.example.FoodHub.entity.RestaurantTable;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.RestaurantOrderMapper;
import com.example.FoodHub.repository.OrderItemRepository;
import com.example.FoodHub.repository.RestaurantOrderRepository;
import com.example.FoodHub.repository.RestaurantTableRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RestaurantOrderService {
    RestaurantOrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    RestaurantOrderMapper orderMapper;
    RestaurantTableRepository tableRepository;

    public List<RestaurantOrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toRestaurantOrderResponse).toList();
    }

    public List<RestaurantOrderResponse> getOrderById() {

        return orderRepository.findAll().stream()
                .map(orderMapper::toRestaurantOrderResponse)
                .toList();
    }

    public RestaurantOrderResponse getOrdersByOrderId(Integer id) {
        log.info("Fetching orders for order ID: {}", id);
        return orderRepository.findById(id)
                .map(orderMapper::toRestaurantOrderResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
    }

    public RestaurantOrderResponse createOrder(RestaurantOrderRequest request) {
        log.info("Creating new order for table: {}", request.getTableId());
        RestaurantOrder order = orderMapper.toRestaurantOrder(request);
        orderRepository.save(order);
        return orderMapper.toRestaurantOrderResponse(order);
    }

    public RestaurantOrderResponse updateOrder(Integer orderId, RestaurantOrderRequest request) {
        log.info("Updating order with ID: {}", orderId);
        RestaurantOrder existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        // Update the order details
        existingOrder.setTable(tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED)));
        existingOrder.setStatus(request.getStatus());
//        existingOrder.setTotalPrice(request.getTotalPrice());

        // Save the updated order
        orderRepository.save(existingOrder);
        return orderMapper.toRestaurantOrderResponse(existingOrder);
    }

    public List<RestaurantOrderResponse> getOrderByTableNumber(String tableNumber) {
        log.info("Fetching orders for table number: {}", tableNumber);
        RestaurantTable table = tableRepository.findByTableNumber(tableNumber)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
        List<RestaurantOrder> order = orderRepository.findByTableId(table.getId());
        return order.stream()
                .map(orderMapper::toRestaurantOrderResponse)
                .toList();
    }

    public List<RestaurantOrderResponse> getOrdersByStatus(String status) {
        log.info("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(orderMapper::toRestaurantOrderResponse)
                .toList();
    }

    public List<RestaurantOrderResponse> getOrdersByTableNumberAndStatus(String tableNumber, String status) {
        log.info("Fetching orders for table number: {} with status: {}", tableNumber, status);
        RestaurantTable table = tableRepository.findByTableNumber(tableNumber)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
        List<RestaurantOrder> orders = orderRepository.findByTableId(table.getId());
        return orders.stream()
                .filter(order -> order.getStatus().equals(status))
                .map(orderMapper::toRestaurantOrderResponse)
                .toList();
    }
}
