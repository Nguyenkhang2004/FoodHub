package com.example.FoodHub.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShiftResponse {
    Integer id; // Shift ID
    String name; // User username
    String role; // Role name
    String date; // Work date (YYYY-MM-DD)
    String shift; // "morning", "afternoon", "night"
}
