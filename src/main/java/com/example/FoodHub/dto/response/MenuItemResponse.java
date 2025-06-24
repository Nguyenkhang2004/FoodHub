
package com.example.FoodHub.dto.response;

import com.example.FoodHub.entity.Category;
import com.example.FoodHub.entity.MenuItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Integer id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private String status;
    private Set<String> categoryNames;
    private List<Integer> categoryIds;

    public MenuItemResponse(MenuItem menuItem) {
        this.id = menuItem.getId();
        this.name = menuItem.getName();
        this.description = menuItem.getDescription();
        this.imageUrl = menuItem.getImageUrl();
        this.price = menuItem.getPrice();
        this.status = menuItem.getStatus();
        this.categoryNames = menuItem.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
        this.categoryIds = (menuItem.getCategories() != null)
                ? menuItem.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toList())
                : List.of();

    }
}
