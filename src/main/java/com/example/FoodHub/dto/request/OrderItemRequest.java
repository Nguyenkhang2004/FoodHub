package com.example.FoodHub.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemRequest {
    @NotNull(message = "MENU_ITEM_ID_REQUIRED")
    @Positive(message = "MENU_ITEM_ID_INVALID")
    private Integer menuItemId;

    @NotNull(message = "QUANTITY_REQUIRED")
    @Min(value = 1, message = "QUANTITY_INVALID")
    @Max(value = 99, message = "QUANTITY_TOO_LARGE")
    private Integer quantity;

//    @NotNull(message = "PRICE_REQUIRED")
//    @DecimalMin(value = "0.0", inclusive = false, message = "PRICE_INVALID")
//    @Digits(integer = 10, fraction = 2, message = "PRICE_FORMAT_INVALID")
//    private BigDecimal price;

    @Pattern(regexp = "PENDING|CONFIRMED|READY|CANCELLED|COMPLETED", message = "ORDER_ITEM_STATUS_INVALID")
    private String status = "PENDING";

}
