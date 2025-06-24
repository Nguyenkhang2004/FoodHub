package com.example.FoodHub.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class InvoiceResponse {
    private Integer orderId;
    private BigDecimal amount;
    private String paymentMethod;
    @JsonProperty("transaction_id")
    private String transactionId;
    private String status;
    private Instant paymentDate; // Giữ nguyên Instant (UTC)
    private String formattedPaymentDate; // Thêm trường mới, định dạng theo UTC+7
    private String tableNumber;
    private String customerName;
    private String customerEmail;
    private List<Map<String, Object>> orderItems; // {itemName, quantity, price}
}

//caams sửa