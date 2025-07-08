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

    @Size(max = 500, message = "NOTE_TOO_LONG")
    private String note;

    @Pattern(regexp = "PENDING|CONFIRMED|READY|CANCELLED|COMPLETED", message = "ORDER_ITEM_STATUS_INVALID")
    private String status = "PENDING";

}
