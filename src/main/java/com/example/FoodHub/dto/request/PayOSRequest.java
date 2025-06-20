package com.example.FoodHub.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayOSRequest {
    Integer orderId;
    String code;
    String id;
    String status;
    boolean cancel;
    Integer orderCode;
}
