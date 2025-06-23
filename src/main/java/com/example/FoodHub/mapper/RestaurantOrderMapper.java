package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.OrderItemRequest;
import com.example.FoodHub.dto.request.RestaurantOrderRequest;
import com.example.FoodHub.dto.response.OrderItemResponse;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.entity.OrderItem;
import com.example.FoodHub.entity.RestaurantOrder;

import java.time.Instant;
import java.time.ZoneId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {PaymentMapper.class})
public interface RestaurantOrderMapper {
    @Mapping(source = "table.id", target = "tableId")
    @Mapping(source = "table.tableNumber", target = "tableNumber")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    RestaurantOrderResponse toRestaurantOrderResponse(RestaurantOrder order);
    @Mapping(source = "table.id", target = "tableId")
    @Mapping(source = "table.tableNumber", target = "tableNumber")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(expression = "java(order.getPayment() != null && \"PAID\".equals(order.getPayment().getStatus()) ? order.getPayment().getPaymentMethod() : null)", target = "paymentMethod")
    RestaurantOrderResponse toRestaurantOrderResponse1(RestaurantOrder order);



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    RestaurantOrder toRestaurantOrder(RestaurantOrderRequest restaurantOrderRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    void updateOrder(@MappingTarget RestaurantOrder order, RestaurantOrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)  // Sẽ được set trong service
    @Mapping(target = "menuItem", ignore = true)  // Sẽ được set trong service
    OrderItem toOrderItem(OrderItemRequest orderItemRequest);
    // Mapping từ OrderItem → OrderItemResponse
    @Mapping(source = "menuItem.id", target = "menuItemId")
    @Mapping(source = "menuItem.name", target = "menuItemName")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
    // Mapping từ OrderItemRequest → OrderItem
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)  // Sẽ được set trong service
    @Mapping(target = "menuItem", ignore = true)  // Sẽ được set trong service
    void updateOrderItem(@MappingTarget OrderItem orderItem, OrderItemRequest orderItemRequest);

}
