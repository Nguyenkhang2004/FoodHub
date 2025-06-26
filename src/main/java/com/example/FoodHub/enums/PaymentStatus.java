package com.example.FoodHub.enums;

import java.util.List;

public enum PaymentStatus {
    PENDING,
    UNPAID,
    PAID,
    CANCELLED,
    FAILED,
    REFUNDED
    ;
    public static List<String> cancellableStatuses() {
        return List.of(PENDING.name(), UNPAID.name(), FAILED.name());
    }
}