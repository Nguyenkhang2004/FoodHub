package com.example.FoodHub.dto.request;

import lombok.Data;

@Data
public class PaymentRequest {
    private Integer orderId;
    private String paymentMethod; // CASH hoặc VNPAY
}