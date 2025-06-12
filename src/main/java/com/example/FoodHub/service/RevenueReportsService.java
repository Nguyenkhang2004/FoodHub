//package com.example.FoodHub.service;
//
//import com.example.FoodHub.dto.response.RevenueReportResponse;
//import com.example.FoodHub.dto.response.TopDishResponse;
//import com.example.FoodHub.repository.OrderItemRepository;
//import com.example.FoodHub.repository.RestaurantOrderRepository;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.time.temporal.ChronoUnit;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class RevenueReportsService {
//    RestaurantOrderRepository orderRepository;
//    OrderItemRepository orderItemRepository;
//
//    public RevenueReportResponse getDashboardData(String period) {
//        Instant start, end;
//        List<String> dailyLabels = new ArrayList<>();
//        LocalDate today = LocalDate.now();
//
//        switch (period) {
//            case "today":
//                start = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
//                end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
//                dailyLabels = List.of("6h", "9h", "12h", "15h", "18h", "21h", "24h");
//                break;
//            case "week":
//                start = today.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant();
//                end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
//                dailyLabels = List.of("T2", "T3", "T4", "T5", "T6", "T7", "CN");
//                break;
//            case "month":
//                start = today.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
//                end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
//                dailyLabels = generateMonthLabels(today);
//                break;
//            case "quarter":
//                start = today.withDayOfMonth(1).minusMonths(2).atStartOfDay(ZoneId.systemDefault()).toInstant();
//                end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
//                dailyLabels = List.of("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12");
//                break;
//            default:
//                throw new IllegalArgumentException("Invalid period: " + period);
//        }
//
//        BigDecimal revenue = orderRepository.findTotalRevenueByPeriod(start, end);
//        Long orders = orderRepository.countOrdersByPeriod(start, end);
//
//        // Gọi query tương ứng với từng period
//        List<Object[]> dailyRevenueData = getDailyRevenueData(period, start, end);
//        List<BigDecimal> dailyRevenue = new ArrayList<>(Collections.nCopies(dailyLabels.size(), BigDecimal.ZERO));
//
//        // Xử lý dữ liệu revenue theo từng ngày
//        for (Object[] data : dailyRevenueData) {
//            try {
//                int index = getIndexForDailyLabel(data[0], period, start);
//                if (index >= 0 && index < dailyLabels.size()) {
//                    dailyRevenue.set(index, (BigDecimal) data[1]);
//                }
//            } catch (Exception e) {
//                log.warn("Error processing daily revenue data: {}, period: {}", data[0], period, e);
//            }
//        }
//
//        List<TopDishResponse> topDishes = orderItemRepository.findTopDishesByPeriod(start, end)
//                .stream()
//                .limit(5)
//                .map(data -> {
//                    TopDishResponse dish = new TopDishResponse();
//                    dish.setName((String) data[0]);
//                    dish.setQuantity(((Number) data[1]).longValue());
//                    dish.setRevenue((BigDecimal) data[2]);
//                    return dish;
//                })
//                .collect(Collectors.toList());
//
//        RevenueReportResponse response = new RevenueReportResponse();
//        response.setRevenue(revenue != null ? revenue : BigDecimal.ZERO);
//        response.setOrders(orders != null ? orders : 0L);
//        response.setDailyRevenue(dailyRevenue);
//        response.setDailyLabels(dailyLabels);
//        response.setTopDishes(topDishes);
//
//        return response;
//    }
//
//    private List<Object[]> getDailyRevenueData(String period, Instant start, Instant end) {
//        switch (period) {
//            case "today":
//                return orderRepository.findHourlyRevenueByPeriod(start, end);
//            case "week":
//                return orderRepository.findDailyRevenueByPeriodForWeek(start, end);
//            case "month":
//                return orderRepository.findDailyRevenueByPeriodForMonth(start, end);
//            case "quarter":
//                return orderRepository.findWeeklyRevenueByPeriod(start, end);
//            default:
//                return new ArrayList<>();
//        }
//    }
//
//    private List<String> generateMonthLabels(LocalDate today) {
//        int daysInMonth = today.lengthOfMonth();
//        List<String> labels = new ArrayList<>();
//        for (int i = 1; i <= daysInMonth; i++) {
//            labels.add(String.valueOf(i));
//        }
//        return labels;
//    }
//
//    private int getIndexForDailyLabel(Object timeData, String period, Instant start) {
//        if ("today".equals(period)) {
//            int hour = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());
//            if (hour <= 6) return 0;
//            if (hour <= 9) return 1;
//            if (hour <= 12) return 2;
//            if (hour <= 15) return 3;
//            if (hour <= 18) return 4;
//            if (hour <= 21) return 5;
//            return 6;
//        } else if ("week".equals(period)) {
//            // timeData sẽ là số ngày từ đầu tuần (1=Monday, 7=Sunday)
//            int dayOfWeek = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());
//            // Convert: 1=T2, 2=T3, ..., 6=T7, 7=CN
//            if (dayOfWeek >= 1 && dayOfWeek <= 7) {
//                return dayOfWeek == 7 ? 6 : dayOfWeek - 1; // Sunday = 6, Monday-Saturday = 0-5
//            }
//            return -1;
//        } else if ("month".equals(period)) {
//            int day = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());
//            return day - 1; // Ngày 1 -> index 0
//        } else if ("quarter".equals(period)) {
//            // timeData sẽ là số tuần trong quý (1-12)
//            int week = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());
//            return week - 1; // Tuần 1 -> index 0
//        }
//        return -1;
//    }
//}
// RevenueReportsService.java
package com.example.FoodHub.service;

import com.example.FoodHub.dto.response.RevenueReportResponse;
import com.example.FoodHub.dto.response.TopDishResponse;
import com.example.FoodHub.repository.OrderItemRepository;
import com.example.FoodHub.repository.RestaurantOrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RevenueReportsService {
    RestaurantOrderRepository orderRepository;
    OrderItemRepository orderItemRepository;

    public RevenueReportResponse getDashboardData(String period, String specificDate) {
        Instant start, end, prevStart, prevEnd;
        List<String> dailyLabels = new ArrayList<>();
        LocalDate today = LocalDate.now();

        switch (period) {
            case "today":
                start = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
                end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                prevStart = today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                prevEnd = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
                dailyLabels = List.of("6h", "9h", "12h", "15h", "18h", "21h", "24h");
                break;
            case "week":
                start = today.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant();
                end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                prevStart = today.minusDays(13).atStartOfDay(ZoneId.systemDefault()).toInstant();
                prevEnd = today.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant();
                dailyLabels = List.of("T2", "T3", "T4", "T5", "T6", "T7", "CN");
                break;
            case "month":
                start = today.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                prevStart = today.minusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                prevEnd = today.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                dailyLabels = generateMonthLabels(today);
                break;
            case "year":
                start = today.withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                prevStart = today.minusYears(1).withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                prevEnd = today.withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                dailyLabels = List.of("Th1", "Th2", "Th3", "Th4", "Th5", "Th6", "Th7", "Th8", "Th9", "Th10", "Th11", "Th12");
                break;
            case "specific":
                if (specificDate == null) {
                    throw new IllegalArgumentException("Specific date is required for period 'specific'");
                }
                LocalDate specific = LocalDate.parse(specificDate);
                start = specific.atStartOfDay(ZoneId.systemDefault()).toInstant();
                end = specific.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                prevStart = specific.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
                prevEnd = specific.atStartOfDay(ZoneId.systemDefault()).toInstant();
                dailyLabels = List.of("6h", "9h", "12h", "15h", "18h", "21h", "24h");
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        // Tính tổng doanh thu và số đơn hàng

        Long orders = orderRepository.countOrdersByPeriod(start, end);

        BigDecimal revenue = orderRepository.findTotalRevenueByPeriod(start, end)
                .orElse(BigDecimal.ZERO);

        // Tính doanh thu trước đó
        BigDecimal previousRevenue = orderRepository.findTotalRevenueByPeriod(prevStart, prevEnd)
                .orElse(BigDecimal.ZERO);


        // Tính doanh thu theo ngày/giờ/tuần
        List<Object[]> dailyRevenueData = getDailyRevenueData(period, start, end);
        List<BigDecimal> dailyRevenue = new ArrayList<>(Collections.nCopies(dailyLabels.size(), BigDecimal.ZERO));
        for (Object[] data : dailyRevenueData) {
            try {
                int index = getIndexForDailyLabel(data[0], period, start);
                if (index >= 0 && index < dailyLabels.size()) {
                    dailyRevenue.set(index, (BigDecimal) data[1]);
                }
            } catch (Exception e) {
                log.warn("Error processing daily revenue data: {}, period: {}", data[0], period, e);
            }
        }

        // Tính doanh thu theo phương thức thanh toán
        List<Object[]> paymentMethodData = orderRepository.findRevenueByPaymentMethod(start, end);
        List<String> paymentMethodLabels = new ArrayList<>();
        List<BigDecimal> paymentMethodRevenue = new ArrayList<>();
        BigDecimal vnpayRevenue = BigDecimal.ZERO, cashRevenue = BigDecimal.ZERO, moreRevenue = BigDecimal.ZERO;
        for (Object[] data : paymentMethodData) {
            String method = (String) data[0];
            BigDecimal amount = (BigDecimal) data[1];
            if ("VNPAY".equalsIgnoreCase(method)) {
                paymentMethodLabels.add("VNPAY");
                paymentMethodRevenue.add(amount);
                vnpayRevenue = amount;
            } else if ("CASH".equalsIgnoreCase(method)) {
                paymentMethodLabels.add("CASH");
                paymentMethodRevenue.add(amount);
                cashRevenue = amount;
            } else {
                moreRevenue = moreRevenue.add(amount);
            }
        }
        if (moreRevenue.compareTo(BigDecimal.ZERO) > 0) {
            paymentMethodLabels.add("MORE");
            paymentMethodRevenue.add(moreRevenue);
        }

        // Tính doanh thu theo loại đơn hàng
        List<Object[]> orderTypeData = orderRepository.findRevenueByOrderType(start, end);
        List<String> orderTypeLabels = new ArrayList<>();
        List<BigDecimal> orderTypeRevenue = new ArrayList<>();
        BigDecimal dineInRevenue = BigDecimal.ZERO, takeAwayRevenue = BigDecimal.ZERO, moreOrderRevenue = BigDecimal.ZERO;
        for (Object[] data : orderTypeData) {
            String type = (String) data[0];
            BigDecimal amount = (BigDecimal) data[1];
            if ("DINE_IN".equalsIgnoreCase(type)) {
                orderTypeLabels.add("DINE_IN");
                orderTypeRevenue.add(amount);
                dineInRevenue = amount;
            } else if ("TAKEAWAY".equalsIgnoreCase(type)) {
                orderTypeLabels.add("TAKEAWAY");
                orderTypeRevenue.add(amount);
                takeAwayRevenue = amount;
            } else {
                moreOrderRevenue = moreOrderRevenue.add(amount);
            }
        }
        if (moreOrderRevenue.compareTo(BigDecimal.ZERO) > 0) {
            orderTypeLabels.add("MORE");
            orderTypeRevenue.add(moreOrderRevenue);
        }

        // Tính top món ăn bán chạy
        List<TopDishResponse> topDishes = orderItemRepository.findTopDishesByPeriod(start, end)
                .stream()
                .limit(5)
                .map(data -> {
                    TopDishResponse dish = new TopDishResponse();
                    dish.setName((String) data[0]);
                    dish.setQuantity(((Number) data[1]).longValue());
                    dish.setRevenue((BigDecimal) data[2]);
                    return dish;
                })
                .collect(Collectors.toList());

        // Xử lý dữ liệu rỗng
        if (revenue == null && orders == null && topDishes.isEmpty() && dailyRevenue.stream().allMatch(v -> v.compareTo(BigDecimal.ZERO) == 0)) {
            log.info("No data found for period: {}, specificDate: {}", period, specificDate);
        }

        RevenueReportResponse response = new RevenueReportResponse();
        response.setRevenue(revenue.longValue());
        response.setPreviousRevenue(previousRevenue.longValue());
        response.setOrders(orders != null ? orders : 0L);
        response.setDailyRevenue(dailyRevenue);
        response.setDailyLabels(dailyLabels);
        response.setTopDishes(topDishes);
        response.setPaymentMethodLabels(paymentMethodLabels);
        response.setPaymentMethodRevenue(paymentMethodRevenue);
        response.setOrderTypeLabels(orderTypeLabels);
        response.setOrderTypeRevenue(orderTypeRevenue);
        return response;
    }

    private List<Object[]> getDailyRevenueData(String period, Instant start, Instant end) {
        switch (period) {
            case "today":
            case "specific":
                return orderRepository.findHourlyRevenueByPeriod(start, end);
            case "week":
                return orderRepository.findDailyRevenueByPeriodForWeek(start, end);
            case "month":
                return orderRepository.findDailyRevenueByPeriodForMonth(start, end);
            case "year":
                return orderRepository.findMonthlyRevenueByPeriod(start, end);
            default:
                return new ArrayList<>();
        }
    }

    private List<String> generateMonthLabels(LocalDate today) {
        int daysInMonth = today.lengthOfMonth();
        List<String> labels = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            labels.add(String.valueOf(i));
        }
        return labels;
    }

    private int getIndexForDailyLabel(Object timeData, String period, Instant start) {
        if ("today".equals(period) || "specific".equals(period)) {
            int hour = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());
            if (hour <= 6) return 0;
            if (hour <= 9) return 1;
            if (hour <= 12) return 2;
            if (hour <= 15) return 3;
            if (hour <= 18) return 4;
            if (hour <= 21) return 5;
            return 6;
        } else if ("week".equals(period)) {
            int dayOfWeek = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());
            return dayOfWeek == 7 ? 6 : dayOfWeek - 1; // Sunday = 6, Monday-Saturday = 0-5
        } else if ("month".equals(period)) {
            int day = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());
            return day - 1; // Ngày 1 -> index 0
        } else if ("year".equals(period)) {
            int month = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());
            return month - 1;
        }
        return -1;
    }
}