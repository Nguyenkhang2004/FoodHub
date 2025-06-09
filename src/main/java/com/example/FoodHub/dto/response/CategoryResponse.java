package com.example.FoodHub.dto.response;

import com.example.FoodHub.entity.Category;
import lombok.Data;

@Data
public class CategoryResponse {
    private Integer id;
    private String name;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }
}