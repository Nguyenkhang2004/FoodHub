package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.entity.MenuItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {
    MenuItemResponse toMenuItemResponse(MenuItem menuItem);
}
