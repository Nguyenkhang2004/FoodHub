package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.entity.MenuItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-10T09:02:49+0700",
    comments = "version: 1.6.0, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class MenuItemMapperImpl implements MenuItemMapper {

    @Override
    public MenuItemResponse toMenuItemResponse(MenuItem menuItem) {
        if ( menuItem == null ) {
            return null;
        }

        MenuItemResponse.MenuItemResponseBuilder menuItemResponse = MenuItemResponse.builder();

        menuItemResponse.status( menuItem.getStatus() );
        menuItemResponse.id( menuItem.getId() );
        menuItemResponse.name( menuItem.getName() );
        menuItemResponse.description( menuItem.getDescription() );
        menuItemResponse.imageUrl( menuItem.getImageUrl() );
        menuItemResponse.price( menuItem.getPrice() );

        menuItemResponse.categoryNames( menuItem.getCategories().stream().map(category -> category.getName()).collect(java.util.stream.Collectors.toList()) );

        return menuItemResponse.build();
    }
}
