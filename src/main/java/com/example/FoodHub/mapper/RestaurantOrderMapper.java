package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.RestaurantOrderRequest;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.entity.RestaurantOrder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RestaurantOrderMapper {
    RestaurantOrderResponse toRestaurantOrderResponse(RestaurantOrder restaurantOrder);
    RestaurantOrder toRestaurantOrder(RestaurantOrderRequest restaurantOrderRequest);
    void updateOrder(@MappingTarget RestaurantOrder order, RestaurantOrderRequest request);
}
