package com.example.FoodHub.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestaurantOrderRequest {
    @NotNull
    Integer tableId;

    @NotNull
    Integer userId;

    String note;

    String orderType;

    String status;

    @NotNull
    Set<OrderItemRequest> orderItems;
}

