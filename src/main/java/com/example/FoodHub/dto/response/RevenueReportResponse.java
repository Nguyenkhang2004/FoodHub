package com.example.FoodHub.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RevenueReportResponse {
    private long revenue;
    private long previousRevenue;
    private Long orders;
    private List<BigDecimal> dailyRevenue;
    private List<String> dailyLabels;
    private List<TopDishResponse> topDishes;
    private List<String> paymentMethodLabels; // Thêm: ["VNPAY", "CASH", "MORE"]
    private List<BigDecimal> paymentMethodRevenue; // Thêm: Doanh thu theo phương thức thanh toán
    private List<String> orderTypeLabels; // Thêm: ["DINE_IN", "TAKE_AWAY", "MORE"]
    private List<BigDecimal> orderTypeRevenue;
}
