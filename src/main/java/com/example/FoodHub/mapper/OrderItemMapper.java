package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.OrderItemRequest;
import com.example.FoodHub.dto.response.OrderItemResponse;
import com.example.FoodHub.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)  // Sẽ được set trong service
    @Mapping(target = "menuItem", ignore = true)  // Sẽ được set trong service
    @Mapping(target = "price", ignore = true)  // Sẽ được tính toán trong service
    OrderItem toOrderItem(OrderItemRequest orderItemRequest);
    // Mapping từ OrderItem → OrderItemResponse
    @Mapping(source = "menuItem.id", target = "menuItemId")
    @Mapping(source = "menuItem.name", target = "menuItemName")
    // Lưu ý: price trong OrderItemResponse sẽ là tổng tiền (quantity × unit price)
    // không phải unit price từ MenuItem
    @Mapping(source = "price", target = "price")  // Giá đã tính toán trong OrderItem
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);    void update(@MappingTarget OrderItem orderItem, OrderItemRequest orderItemRequest);
}
