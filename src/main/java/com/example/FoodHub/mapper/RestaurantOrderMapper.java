package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.OrderItemRequest;
import com.example.FoodHub.dto.request.RestaurantOrderRequest;
import com.example.FoodHub.dto.response.OrderItemResponse;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.entity.OrderItem;
import com.example.FoodHub.entity.RestaurantOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RestaurantOrderMapper {
    @Mapping(source = "table.id", target = "tableId")
    @Mapping(source = "table.tableNumber", target = "tableNumber")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "orderType", target = "orderType")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "orderItems", target = "orderItems")
    RestaurantOrderResponse toRestaurantOrderResponse(RestaurantOrder order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(source = "status", target = "status")
    @Mapping(source = "orderType", target = "orderType")
    @Mapping(source = "note", target = "note")
    RestaurantOrder toRestaurantOrder(RestaurantOrderRequest restaurantOrderRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(source = "status", target = "status")
    @Mapping(source = "orderType", target = "orderType")
    @Mapping(source = "note", target = "note")
    void updateOrder(@MappingTarget RestaurantOrder order, RestaurantOrderRequest request);

    @Mapping(source = "quantity", target = "quantity")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}
