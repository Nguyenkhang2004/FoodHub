package com.example.FoodHub.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
    @Positive(message = "TABLE_ID_INVALID")
    private Integer tableId;

    @Positive(message = "USER_ID_INVALID")
    private Integer userId;

    @Pattern(regexp = "DINE_IN|TAKEAWAY|DELIVERY", message = "ORDER_TYPE_INVALID")
    private String orderType;

    @Pattern(regexp = "PENDING|CONFIRMED|READY|CANCELLED|COMPLETED", message = "ORDER_STATUS_INVALID")
    private String status = "PENDING";

    @NotNull(message = "ORDER_ITEMS_REQUIRED")
    @NotEmpty(message = "ORDER_ITEMS_REQUIRED")
    @Valid
    private Set<OrderItemRequest> orderItems;

    private PaymentRequest payment;
}

