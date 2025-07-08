package com.example.FoodHub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScanQRResponse {
    String token;
    String tableNumber;
    Integer orderId; // ID đơn hàng (nếu có)
    Instant expiryTime;
}
