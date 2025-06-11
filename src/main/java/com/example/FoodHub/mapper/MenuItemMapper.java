package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.MenuItemRequest;
import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.entity.Category;
import com.example.FoodHub.entity.MenuItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(target = "categoryNames", source = "categories", qualifiedByName = "toCategoryNames")
    @Mapping(target = "categoryIds", source = "categories", qualifiedByName = "toCategoryIds")
    MenuItemResponse toMenuItemResponse(MenuItem menuItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "status", constant = "AVAILABLE")
    MenuItem toMenuItem(MenuItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateMenuItemFromRequest(MenuItemRequest request, @MappingTarget MenuItem menuItem);

    @Named("toCategoryNames")
    default Set<String> toCategoryNames(Set<Category> categories) {
        return categories.stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
    }

    @Named("toCategoryIds")
    default List<Integer> toCategoryIds(Set<Category> categories) {
        return categories.stream()
                .map(Category::getId)
                .collect(Collectors.toList());
    }
}