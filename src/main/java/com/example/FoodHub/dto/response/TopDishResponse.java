package com.example.FoodHub.dto.response;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class TopDishResponse {
    private String name;
    private Long quantity;
    private BigDecimal revenue;
}
