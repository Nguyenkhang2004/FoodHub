package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.OrderItemRequest;
import com.example.FoodHub.dto.request.RestaurantOrderRequest;
import com.example.FoodHub.dto.response.OrderItemResponse;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.entity.MenuItem;
import com.example.FoodHub.entity.OrderItem;
import com.example.FoodHub.entity.Payment;
import com.example.FoodHub.entity.RestaurantOrder;
import com.example.FoodHub.entity.RestaurantTable;
import com.example.FoodHub.entity.User;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T17:12:32+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class RestaurantOrderMapperImpl implements RestaurantOrderMapper {

    @Autowired
    private PaymentMapper paymentMapper;

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
        restaurantOrderResponse.payment( paymentMapper.toPaymentResponse( order.getPayment() ) );
        restaurantOrderResponse.paymentStatus( orderPaymentStatus( order ) );
        restaurantOrderResponse.id( order.getId() );
        restaurantOrderResponse.status( order.getStatus() );
        restaurantOrderResponse.orderType( order.getOrderType() );
        restaurantOrderResponse.createdAt( order.getCreatedAt() );
        restaurantOrderResponse.updatedAt( order.getUpdatedAt() );
        restaurantOrderResponse.note( order.getNote() );
        restaurantOrderResponse.totalAmount( order.getTotalAmount() );
        restaurantOrderResponse.orderItems( orderItemSetToOrderItemResponseSet( order.getOrderItems() ) );

        return restaurantOrderResponse.build();
    }

    @Override
    public RestaurantOrderResponse toRestaurantOrderResponsePaidOnly(RestaurantOrder order) {
        if ( order == null ) {
            return null;
        }

        RestaurantOrderResponse.RestaurantOrderResponseBuilder restaurantOrderResponse = RestaurantOrderResponse.builder();

        restaurantOrderResponse.tableId( orderTableId( order ) );
        restaurantOrderResponse.tableNumber( orderTableTableNumber( order ) );
        restaurantOrderResponse.userId( orderUserId( order ) );
        restaurantOrderResponse.username( orderUserUsername( order ) );
        restaurantOrderResponse.id( order.getId() );
        restaurantOrderResponse.status( order.getStatus() );
        restaurantOrderResponse.orderType( order.getOrderType() );
        restaurantOrderResponse.createdAt( order.getCreatedAt() );
        restaurantOrderResponse.updatedAt( order.getUpdatedAt() );
        restaurantOrderResponse.note( order.getNote() );
        restaurantOrderResponse.totalAmount( order.getTotalAmount() );
        restaurantOrderResponse.orderItems( orderItemSetToOrderItemResponseSet( order.getOrderItems() ) );
        restaurantOrderResponse.payment( paymentMapper.toPaymentResponse( order.getPayment() ) );

        restaurantOrderResponse.paymentMethod( order.getPayment() != null && "PAID".equals(order.getPayment().getStatus()) ? order.getPayment().getPaymentMethod() : null );

        return restaurantOrderResponse.build();
    }

    @Override
    public RestaurantOrder toRestaurantOrder(RestaurantOrderRequest restaurantOrderRequest) {
        if ( restaurantOrderRequest == null ) {
            return null;
        }

        RestaurantOrder restaurantOrder = new RestaurantOrder();

        restaurantOrder.setStatus( restaurantOrderRequest.getStatus() );
        restaurantOrder.setOrderType( restaurantOrderRequest.getOrderType() );
        restaurantOrder.setPayment( paymentMapper.toPayment( restaurantOrderRequest.getPayment() ) );
        restaurantOrder.setNote( restaurantOrderRequest.getNote() );

        return restaurantOrder;
    }

    @Override
    public void updateOrder(RestaurantOrder order, RestaurantOrderRequest request) {
        if ( request == null ) {
            return;
        }

        order.setStatus( request.getStatus() );
        order.setOrderType( request.getOrderType() );
        if ( request.getPayment() != null ) {
            if ( order.getPayment() == null ) {
                order.setPayment( new Payment() );
            }
            paymentMapper.updatePaymentFromRequest( request.getPayment(), order.getPayment() );
        }
        else {
            order.setPayment( null );
        }
        order.setNote( request.getNote() );
    }

    @Override
    public OrderItem toOrderItem(OrderItemRequest orderItemRequest) {
        if ( orderItemRequest == null ) {
            return null;
        }

        OrderItem orderItem = new OrderItem();

        orderItem.setQuantity( orderItemRequest.getQuantity() );
        orderItem.setNote( orderItemRequest.getNote() );
        orderItem.setStatus( orderItemRequest.getStatus() );

        return orderItem;
    }

    @Override
    public OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        OrderItemResponse.OrderItemResponseBuilder orderItemResponse = OrderItemResponse.builder();

        orderItemResponse.menuItemId( orderItemMenuItemId( orderItem ) );
        orderItemResponse.menuItemName( orderItemMenuItemName( orderItem ) );
        orderItemResponse.id( orderItem.getId() );
        orderItemResponse.quantity( orderItem.getQuantity() );
        orderItemResponse.price( orderItem.getPrice() );
        orderItemResponse.status( orderItem.getStatus() );
        orderItemResponse.note( orderItem.getNote() );

        return orderItemResponse.build();
    }

    @Override
    public void updateOrderItem(OrderItem orderItem, OrderItemRequest orderItemRequest) {
        if ( orderItemRequest == null ) {
            return;
        }

        orderItem.setQuantity( orderItemRequest.getQuantity() );
        orderItem.setNote( orderItemRequest.getNote() );
        orderItem.setStatus( orderItemRequest.getStatus() );
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

    private String orderPaymentStatus(RestaurantOrder restaurantOrder) {
        Payment payment = restaurantOrder.getPayment();
        if ( payment == null ) {
            return null;
        }
        return payment.getStatus();
    }

    protected Set<OrderItemResponse> orderItemSetToOrderItemResponseSet(Set<OrderItem> set) {
        if ( set == null ) {
            return null;
        }

        Set<OrderItemResponse> set1 = new LinkedHashSet<OrderItemResponse>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( OrderItem orderItem : set ) {
            set1.add( toOrderItemResponse( orderItem ) );
        }

        return set1;
    }

    private Integer orderItemMenuItemId(OrderItem orderItem) {
        MenuItem menuItem = orderItem.getMenuItem();
        if ( menuItem == null ) {
            return null;
        }
        return menuItem.getId();
    }

    private String orderItemMenuItemName(OrderItem orderItem) {
        MenuItem menuItem = orderItem.getMenuItem();
        if ( menuItem == null ) {
            return null;
        }
        return menuItem.getName();
    }
}
