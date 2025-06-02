package com.example.FoodHub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestaurantOrderResponse {
    private Integer id;
    private String status;
    private String orderType;
    private Instant createdAt;
    private String note;

    private Integer tableId;
    private String tableNumber;

    private Integer userId;
    private String username;

    private Set<OrderItemResponse> orderItems;
}
