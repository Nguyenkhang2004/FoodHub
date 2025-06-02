package com.example.FoodHub.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Integer id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private String status;
}
