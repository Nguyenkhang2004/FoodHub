package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.OrderItemRequest;
import com.example.FoodHub.dto.request.RestaurantOrderRequest;
import com.example.FoodHub.dto.response.OrderItemResponse;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.entity.OrderItem;
import com.example.FoodHub.entity.RestaurantOrder;
import com.example.FoodHub.entity.RestaurantTable;
import com.example.FoodHub.entity.User;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-03T08:25:07+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class RestaurantOrderMapperImpl implements RestaurantOrderMapper {

    @Override
    public RestaurantOrderResponse toRestaurantOrderResponse(RestaurantOrder order) {
        if ( order == null ) {
            return null;
        }

        RestaurantOrderResponse.RestaurantOrderResponseBuilder restaurantOrderResponse = RestaurantOrderResponse.builder();

        restaurantOrderResponse.tableId( orderTableId( order ) );
        restaurantOrderResponse.tableNumber( orderTableTableNumber( order ) );
        restaurantOrderResponse.userId( orderUserId( order ) );
        restaurantOrderResponse.username( orderUserUsername( order ) );
        restaurantOrderResponse.orderItems( orderItemSetToOrderItemResponseSet( order.getOrderItems() ) );
        restaurantOrderResponse.id( order.getId() );
        restaurantOrderResponse.status( order.getStatus() );
        restaurantOrderResponse.orderType( order.getOrderType() );
        restaurantOrderResponse.createdAt( order.getCreatedAt() );
        restaurantOrderResponse.note( order.getNote() );

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

        return restaurantOrder;
    }

    @Override
    public void updateOrder(RestaurantOrder order, RestaurantOrderRequest request) {
        if ( request == null ) {
            return;
        }

        order.setStatus( request.getStatus() );
        order.setNote( request.getNote() );
        order.setOrderType( request.getOrderType() );
        if ( order.getOrderItems() != null ) {
            Set<OrderItem> set = orderItemRequestSetToOrderItemSet( request.getOrderItems() );
            if ( set != null ) {
                order.getOrderItems().clear();
                order.getOrderItems().addAll( set );
            }
            else {
                order.setOrderItems( null );
            }
        }
        else {
            Set<OrderItem> set = orderItemRequestSetToOrderItemSet( request.getOrderItems() );
            if ( set != null ) {
                order.setOrderItems( set );
            }
        }
    }

    private Integer orderTableId(RestaurantOrder restaurantOrder) {
        RestaurantTable table = restaurantOrder.getTable();
        if ( table == null ) {
            return null;
        }
        return table.getId();
    }

    private String orderTableTableNumber(RestaurantOrder restaurantOrder) {
        RestaurantTable table = restaurantOrder.getTable();
        if ( table == null ) {
            return null;
        }
        return table.getTableNumber();
    }

    private Integer orderUserId(RestaurantOrder restaurantOrder) {
        User user = restaurantOrder.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getId();
    }

    private String orderUserUsername(RestaurantOrder restaurantOrder) {
        User user = restaurantOrder.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getUsername();
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
        orderItem.setStatus( orderItemRequest.getStatus() );

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
