package com.example.FoodHub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemResponse {
    Integer id;
    Integer menuItemId;
    String menuItemName; // tiện cho hiển thị
    Integer quantity;
    BigDecimal price;
    String status;
}
