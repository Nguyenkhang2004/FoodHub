package com.example.FoodHub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {
    private Integer orderId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId; // Đảm bảo có trường này
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private String paymentUrl;


}