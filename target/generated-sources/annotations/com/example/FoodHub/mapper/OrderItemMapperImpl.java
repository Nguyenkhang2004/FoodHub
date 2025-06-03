package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.OrderItemRequest;
import com.example.FoodHub.dto.response.OrderItemResponse;
import com.example.FoodHub.entity.OrderItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-03T08:25:07+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class OrderItemMapperImpl implements OrderItemMapper {

    @Override
    public OrderItem toOrderItem(OrderItemRequest orderItemRequest) {
        if ( orderItemRequest == null ) {
            return null;
        }

        OrderItem orderItem = new OrderItem();

        orderItem.setQuantity( orderItemRequest.getQuantity() );
        orderItem.setStatus( orderItemRequest.getStatus() );

        return orderItem;
    }

    @Override
    public OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
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

    @Override
    public void update(OrderItem orderItem, OrderItemRequest orderItemRequest) {
        if ( orderItemRequest == null ) {
            return;
        }

        orderItem.setQuantity( orderItemRequest.getQuantity() );
        orderItem.setStatus( orderItemRequest.getStatus() );
    }
}
