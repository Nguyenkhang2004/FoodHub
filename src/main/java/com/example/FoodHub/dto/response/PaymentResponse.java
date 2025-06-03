package com.example.FoodHub.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentResponse {
    private Integer orderId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}