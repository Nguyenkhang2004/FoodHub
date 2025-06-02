package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.OrderItemRequest;
import com.example.FoodHub.dto.request.RestaurantOrderRequest;
import com.example.FoodHub.dto.response.OrderItemResponse;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.entity.OrderItem;
import com.example.FoodHub.entity.RestaurantOrder;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-02T11:01:04+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class RestaurantOrderMapperImpl implements RestaurantOrderMapper {

    @Override
    public RestaurantOrderResponse toRestaurantOrderResponse(RestaurantOrder restaurantOrder) {
        if ( restaurantOrder == null ) {
            return null;
        }

        RestaurantOrderResponse.RestaurantOrderResponseBuilder restaurantOrderResponse = RestaurantOrderResponse.builder();

        restaurantOrderResponse.id( restaurantOrder.getId() );
        restaurantOrderResponse.status( restaurantOrder.getStatus() );
        restaurantOrderResponse.orderType( restaurantOrder.getOrderType() );
        restaurantOrderResponse.createdAt( restaurantOrder.getCreatedAt() );
        restaurantOrderResponse.note( restaurantOrder.getNote() );
        restaurantOrderResponse.orderItems( orderItemSetToOrderItemResponseSet( restaurantOrder.getOrderItems() ) );

        return restaurantOrderResponse.build();
    }

    @Override
    public RestaurantOrder toRestaurantOrder(RestaurantOrderRequest restaurantOrderRequest) {
        if ( restaurantOrderRequest == null ) {
            return null;
        }

        RestaurantOrder restaurantOrder = new RestaurantOrder();

        restaurantOrder.setStatus( restaurantOrderRequest.getStatus() );
        restaurantOrder.setNote( restaurantOrderRequest.getNote() );
        restaurantOrder.setOrderType( restaurantOrderRequest.getOrderType() );
        restaurantOrder.setOrderItems( orderItemRequestSetToOrderItemSet( restaurantOrderRequest.getOrderItems() ) );

        return restaurantOrder;
    }

    protected OrderItemResponse orderItemToOrderItemResponse(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        OrderItemResponse.OrderItemResponseBuilder orderItemResponse = OrderItemResponse.builder();

        orderItemResponse.id( orderItem.getId() );
        orderItemResponse.quantity( orderItem.getQuantity() );
        orderItemResponse.price( orderItem.getPrice() );
        orderItemResponse.status( orderItem.getStatus() );

        return orderItemResponse.build();
    }

    protected Set<OrderItemResponse> orderItemSetToOrderItemResponseSet(Set<OrderItem> set) {
        if ( set == null ) {
            return null;
        }

        Set<OrderItemResponse> set1 = new LinkedHashSet<OrderItemResponse>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( OrderItem orderItem : set ) {
            set1.add( orderItemToOrderItemResponse( orderItem ) );
        }

        return set1;
    }

    protected OrderItem orderItemRequestToOrderItem(OrderItemRequest orderItemRequest) {
        if ( orderItemRequest == null ) {
            return null;
        }

        OrderItem orderItem = new OrderItem();

        orderItem.setQuantity( orderItemRequest.getQuantity() );
        orderItem.setPrice( orderItemRequest.getPrice() );

        return orderItem;
    }

    protected Set<OrderItem> orderItemRequestSetToOrderItemSet(Set<OrderItemRequest> set) {
        if ( set == null ) {
            return null;
        }

        Set<OrderItem> set1 = new LinkedHashSet<OrderItem>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( OrderItemRequest orderItemRequest : set ) {
            set1.add( orderItemRequestToOrderItem( orderItemRequest ) );
        }

        return set1;
    }
}
