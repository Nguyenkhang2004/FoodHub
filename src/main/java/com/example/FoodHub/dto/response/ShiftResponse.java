package com.example.FoodHub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShiftResponse {
    private Integer id;
    private String name; // User username
    private String role; // Role name
    private String date; // Work date (YYYY-MM-DD)
    private String shift;
    private String area;
    private String startTime;
    private String endTime;
}
