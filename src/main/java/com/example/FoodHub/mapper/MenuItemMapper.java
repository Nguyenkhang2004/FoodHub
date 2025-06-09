package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.entity.Category;
import com.example.FoodHub.entity.MenuItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(source = "categories", target = "categoryNames", qualifiedByName = "mapCategoryNames")
    MenuItemResponse toMenuItemResponse(MenuItem menuItem);

    @Named("mapCategoryNames")
    static List<String> mapCategoryNames(Set<Category> categories) {
        if (categories == null) return List.of();
        return categories.stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }
}
