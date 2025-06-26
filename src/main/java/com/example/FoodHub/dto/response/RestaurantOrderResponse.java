package com.example.FoodHub.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String note;
    private Integer tableId;
    private String tableNumber;
    private Integer userId;
    private String username;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private Set<OrderItemResponse> orderItems;
    private PaymentResponse payment;
    private String paymentStatus; // Thêm trường mới để lưu trạng thái thanh toán
}