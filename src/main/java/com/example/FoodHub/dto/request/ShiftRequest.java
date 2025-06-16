package com.example.FoodHub.dto.request;

import lombok.Data;

@Data
public class ShiftRequest {
    private Integer id;
    private String name; // User username
    private String role; // Role name
    private String date; // Work date (YYYY-MM-DD)
    private String shift;
    private String area;
}
