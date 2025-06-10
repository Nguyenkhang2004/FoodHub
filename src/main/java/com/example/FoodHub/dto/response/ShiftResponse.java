package com.example.FoodHub.dto.response;

import lombok.Data;

@Data
public class ShiftResponse {
    private Integer id;
    private String name; // User username
    private String role; // Role name
    private String date; // Work date (YYYY-MM-DD)
    private String shift;
}
