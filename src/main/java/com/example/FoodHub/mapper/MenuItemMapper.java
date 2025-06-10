package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.entity.MenuItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {
    @Mapping(target = "categoryNames", expression = "java(menuItem.getCategories().stream().map(category -> category.getName()).collect(java.util.stream.Collectors.toList()))")
    @Mapping(target = "status", source = "status") // Hoặc bỏ @Mapping vì tên khớp
    MenuItemResponse toMenuItemResponse(MenuItem menuItem);
}