package com.example.FoodHub.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentResponse {
    private Integer orderId;
    private BigDecimal amount;
    private String paymentMethod;
    @JsonProperty("transaction_id") // Nếu JSON dùng transaction_id
    private String transactionId; // Đảm bảo có trường này
    private String status;
    private Instant createdAt;
    private Instant updatedAt;


    // thằng khang thêm, bố mày đéo biết
    private String paymentUrl;

}