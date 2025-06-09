package com.example.FoodHub.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShiftRequest {
    String name;
    String date;
    String shift; // "morning", "afternoon", "night"
}
