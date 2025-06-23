package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.MenuItemRequest;
import com.example.FoodHub.dto.response.MenuItemResponse;
import com.example.FoodHub.entity.MenuItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-23T07:53:58+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 21.0.3 (Oracle Corporation)"
)
@Component
public class MenuItemMapperImpl implements MenuItemMapper {

    @Override
    public MenuItemResponse toMenuItemResponse(MenuItem menuItem) {
        if ( menuItem == null ) {
            return null;
        }

        MenuItemResponse.MenuItemResponseBuilder menuItemResponse = MenuItemResponse.builder();

        menuItemResponse.categoryNames( toCategoryNames( menuItem.getCategories() ) );
        menuItemResponse.categoryIds( toCategoryIds( menuItem.getCategories() ) );
        menuItemResponse.id( menuItem.getId() );
        menuItemResponse.name( menuItem.getName() );
        menuItemResponse.description( menuItem.getDescription() );
        menuItemResponse.imageUrl( menuItem.getImageUrl() );
        menuItemResponse.price( menuItem.getPrice() );
        menuItemResponse.status( menuItem.getStatus() );

        return menuItemResponse.build();
    }

    @Override
    public MenuItem toMenuItem(MenuItemRequest request) {
        if ( request == null ) {
            return null;
        }

        MenuItem menuItem = new MenuItem();

        menuItem.setName( request.getName() );
        menuItem.setDescription( request.getDescription() );
        menuItem.setPrice( request.getPrice() );

        menuItem.setStatus( "AVAILABLE" );

        return menuItem;
    }

    @Override
    public void updateMenuItemFromRequest(MenuItemRequest request, MenuItem menuItem) {
        if ( request == null ) {
            return;
        }

        menuItem.setName( request.getName() );
        menuItem.setDescription( request.getDescription() );
        menuItem.setPrice( request.getPrice() );
    }
}
