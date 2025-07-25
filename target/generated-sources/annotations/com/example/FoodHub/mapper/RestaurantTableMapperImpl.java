package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.RestaurantTableRequest;
import com.example.FoodHub.dto.response.RestaurantTableResponse;
import com.example.FoodHub.entity.RestaurantTable;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-14T21:10:22+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class RestaurantTableMapperImpl implements RestaurantTableMapper {

    @Override
    public RestaurantTableResponse toRestaurantTableResponse(RestaurantTable restaurantTable) {
        if ( restaurantTable == null ) {
            return null;
        }

        RestaurantTableResponse.RestaurantTableResponseBuilder restaurantTableResponse = RestaurantTableResponse.builder();

        restaurantTableResponse.id( restaurantTable.getId() );
        restaurantTableResponse.tableNumber( restaurantTable.getTableNumber() );
        restaurantTableResponse.qrCode( restaurantTable.getQrCode() );
        restaurantTableResponse.status( restaurantTable.getStatus() );
        restaurantTableResponse.area( restaurantTable.getArea() );

        return restaurantTableResponse.build();
    }

    @Override
    public RestaurantTable toRestaurantTable(RestaurantTableRequest restaurantTableRequest) {
        if ( restaurantTableRequest == null ) {
            return null;
        }

        RestaurantTable restaurantTable = new RestaurantTable();

        restaurantTable.setArea( restaurantTableRequest.getArea() );
        restaurantTable.setTableNumber( restaurantTableRequest.getTableNumber() );
        restaurantTable.setQrCode( restaurantTableRequest.getQrCode() );
        restaurantTable.setStatus( restaurantTableRequest.getStatus() );

        return restaurantTable;
    }

    @Override
    public void updateTable(RestaurantTable restaurantTable, RestaurantTableRequest restaurantTableRequest) {
        if ( restaurantTableRequest == null ) {
            return;
        }

        restaurantTable.setArea( restaurantTableRequest.getArea() );
        restaurantTable.setTableNumber( restaurantTableRequest.getTableNumber() );
        restaurantTable.setQrCode( restaurantTableRequest.getQrCode() );
        restaurantTable.setStatus( restaurantTableRequest.getStatus() );
    }
}
