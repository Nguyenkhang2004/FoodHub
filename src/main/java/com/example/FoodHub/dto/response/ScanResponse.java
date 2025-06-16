package com.example.FoodHub.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScanResponse {
    String sessionToken;
    Instant expiresAt;
    String message;
}
