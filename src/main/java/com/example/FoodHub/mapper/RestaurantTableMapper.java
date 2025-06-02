package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.RestaurantTableRequest;
import com.example.FoodHub.dto.response.RestaurantTableResponse;
import com.example.FoodHub.entity.RestaurantTable;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RestaurantTableMapper {
    RestaurantTableResponse toRestaurantTableResponse(RestaurantTable restaurantTable);
    RestaurantTable toRestaurantTable(RestaurantTableRequest restaurantTableRequest);
    void updateRestaurantTable(@MappingTarget RestaurantTable restaurantTable, RestaurantTableRequest restaurantTableRequest);
}
