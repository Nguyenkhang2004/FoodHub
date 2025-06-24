package com.example.FoodHub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishSalesResponse {
    private List<String> dishNames;
    private List<Long> quantities;
}