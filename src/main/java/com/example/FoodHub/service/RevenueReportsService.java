package com.example.FoodHub.service;

import com.example.FoodHub.dto.response.CategoryResponse;
import com.example.FoodHub.dto.response.DishSalesResponse;
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
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
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
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate today = LocalDate.now(zoneId);
        ZoneId utcZone = ZoneId.of("UTC");

        switch (period) {
            case "today":
                start = today.atStartOfDay(utcZone).toInstant();
                end = today.plusDays(1).atStartOfDay(utcZone).toInstant();
                prevStart = today.minusDays(1).atStartOfDay(utcZone).toInstant();
                prevEnd = today.atStartOfDay(utcZone).toInstant();
                dailyLabels = List.of("6h", "9h", "12h", "15h", "18h", "21h", "24h");
                break;

            case "week":
                // Tìm thứ 2 của tuần hiện tại
                LocalDate mondayOfCurrentWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate sundayOfCurrentWeek = mondayOfCurrentWeek.plusDays(6);

                start = mondayOfCurrentWeek.atStartOfDay(utcZone).toInstant();
                end = sundayOfCurrentWeek.plusDays(1).atStartOfDay(utcZone).toInstant(); // Bao gồm cả Chủ nhật

                // Tuần trước
                LocalDate mondayOfPreviousWeek = mondayOfCurrentWeek.minusDays(7);
                LocalDate sundayOfPreviousWeek = mondayOfPreviousWeek.plusDays(6);
                prevStart = mondayOfPreviousWeek.atStartOfDay(utcZone).toInstant();
                prevEnd = sundayOfPreviousWeek.plusDays(1).atStartOfDay(utcZone).toInstant();

                dailyLabels = List.of("T2", "T3", "T4", "T5", "T6", "T7", "CN");
                break;

            case "month":
                start = today.withDayOfMonth(1).atStartOfDay(utcZone).toInstant();
                // FIX: Lấy đến cuối tháng hiện tại
                end = today.withDayOfMonth(today.lengthOfMonth()).plusDays(1).atStartOfDay(utcZone).toInstant();

                // FIX: Tháng trước cũng lấy đến cuối tháng
                LocalDate lastMonth = today.minusMonths(1);
                prevStart = lastMonth.withDayOfMonth(1).atStartOfDay(utcZone).toInstant();
                prevEnd = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).plusDays(1).atStartOfDay(utcZone).toInstant();

                dailyLabels = generateMonthLabels(today);
                break;

            case "year":
                start = today.withDayOfYear(1).atStartOfDay(utcZone).toInstant();
                // FIX: Lấy đến cuối năm hiện tại
                end = today.withDayOfYear(today.lengthOfYear()).plusDays(1).atStartOfDay(utcZone).toInstant();

                // FIX: Năm trước cũng lấy đến cuối năm
                LocalDate lastYear = today.minusYears(1);
                prevStart = lastYear.withDayOfYear(1).atStartOfDay(utcZone).toInstant();
                prevEnd = lastYear.withDayOfYear(lastYear.lengthOfYear()).plusDays(1).atStartOfDay(utcZone).toInstant();

                dailyLabels = List.of("Th1", "Th2", "Th3", "Th4", "Th5", "Th6", "Th7", "Th8", "Th9", "Th10", "Th11", "Th12");
                break;

            case "specific":
                if (specificDate == null) {
                    throw new IllegalArgumentException("Specific date is required for period 'specific'");
                }
                LocalDate specific = LocalDate.parse(specificDate);
                start = specific.atStartOfDay(utcZone).toInstant();
                end = specific.plusDays(1).atStartOfDay(utcZone).toInstant();
                prevStart = specific.minusDays(1).atStartOfDay(utcZone).toInstant();
                prevEnd = specific.atStartOfDay(utcZone).toInstant();
                dailyLabels = List.of("6h", "8h", "10h", "12h", "14h", "16h", "18h", "20h", "22h", "24h");
                break;

            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        BigDecimal revenue = orderRepository.findTotalRevenueByPeriod(start, end)
                .orElse(BigDecimal.ZERO);
        Long orders = orderRepository.countOrdersByPeriod(start, end);
        BigDecimal previousRevenue = orderRepository.findTotalRevenueByPeriod(prevStart, prevEnd)
                .orElse(BigDecimal.ZERO);

        List<Object[]> dailyRevenueData = getDailyRevenueData(period, start, end);
        List<BigDecimal> dailyRevenue = new ArrayList<>(Collections.nCopies(dailyLabels.size(), BigDecimal.ZERO));
        for (Object[] data : dailyRevenueData) {
            int index = getIndexForDailyLabel(data[0], period, ZonedDateTime.ofInstant(start, ZoneId.of("UTC")));
            if (index >= 0 && index < dailyLabels.size()) {
                dailyRevenue.set(index, (BigDecimal) data[1]);
            }
        }

        List<Object[]> paymentMethodData = orderRepository.findRevenueByPaymentMethod(start, end);
        List<String> paymentMethodLabels = new ArrayList<>();
        List<BigDecimal> paymentMethodRevenue = new ArrayList<>();
        for (Object[] data : paymentMethodData) {
            paymentMethodLabels.add((String) data[0]);
            paymentMethodRevenue.add((BigDecimal) data[1]);
        }

        List<Object[]> orderTypeData = orderRepository.findRevenueByOrderType(start, end);
        List<String> orderTypeLabels = new ArrayList<>();
        List<BigDecimal> orderTypeRevenue = new ArrayList<>();
        for (Object[] data : orderTypeData) {
            orderTypeLabels.add((String) data[0]);
            orderTypeRevenue.add((BigDecimal) data[1]);
        }

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

        List<Object[]> categoryData = orderItemRepository.findQuantityByCategory(start, end, null);
        List<String> categoryLabels = new ArrayList<>();
        List<Long> categoryQuantities = new ArrayList<>();
        for (Object[] data : categoryData) {
            categoryLabels.add((String) data[0]);
            categoryQuantities.add(((Number) data[1]).longValue());
        }

        List<CategoryResponse> categories = orderItemRepository.findAllCategories();

        RevenueReportResponse response = RevenueReportResponse.builder()
                .revenue(revenue.longValue())
                .previousRevenue(previousRevenue.longValue())
                .orders(orders != null ? orders : 0L)
                .dailyRevenue(dailyRevenue)
                .dailyLabels(dailyLabels)
                .topDishes(topDishes)
                .paymentMethodLabels(paymentMethodLabels)
                .paymentMethodRevenue(paymentMethodRevenue)
                .orderTypeLabels(orderTypeLabels)
                .orderTypeRevenue(orderTypeRevenue)
                .categoryLabels(categoryLabels)
                .categoryQuantities(categoryQuantities)
                .categories(categories)
                .build();

        return response;
    }

    public DishSalesResponse getDishSalesData(String period, String specificDate, Integer categoryId) {
        Instant start, end;
        ZoneId utcZone = ZoneId.of("UTC");
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        switch (period) {
            case "today":
                start = today.atStartOfDay(utcZone).toInstant();
                end = today.plusDays(1).atStartOfDay(utcZone).toInstant();
                break;

            case "week":
                // Sử dụng cùng logic với getDashboardData cho consistency
                LocalDate mondayOfCurrentWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate sundayOfCurrentWeek = mondayOfCurrentWeek.plusDays(6);
                start = mondayOfCurrentWeek.atStartOfDay(utcZone).toInstant();
                end = sundayOfCurrentWeek.plusDays(1).atStartOfDay(utcZone).toInstant();
                break;

            case "month":
                start = today.withDayOfMonth(1).atStartOfDay(utcZone).toInstant();
                // FIX: Lấy đến cuối tháng
                end = today.withDayOfMonth(today.lengthOfMonth()).plusDays(1).atStartOfDay(utcZone).toInstant();
                break;

            case "year":
                start = today.withDayOfYear(1).atStartOfDay(utcZone).toInstant();
                // FIX: Lấy đến cuối năm
                end = today.withDayOfYear(today.lengthOfYear()).plusDays(1).atStartOfDay(utcZone).toInstant();
                break;

            case "specific":
                if (specificDate == null) {
                    throw new IllegalArgumentException("Specific date is required for period 'specific'");
                }
                LocalDate specific = LocalDate.parse(specificDate);
                start = specific.atStartOfDay(utcZone).toInstant();
                end = specific.plusDays(1).atStartOfDay(utcZone).toInstant();
                break;

            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }
        List<Object[]> dishData = orderItemRepository.findQuantityByMenuItem(start, end, categoryId);
        List<String> dishNames = new ArrayList<>();
        List<Long> quantities = new ArrayList<>();
        for (Object[] data : dishData) {
            dishNames.add((String) data[0]);
            quantities.add(((Number) data[1]).longValue());
        }

        return DishSalesResponse.builder()
                .dishNames(dishNames)
                .quantities(quantities)
                .build();
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
    private int getIndexForDailyLabel(Object timeData, String period, ZonedDateTime start) {
        if ("today".equals(period) || "specific".equals(period)) {
            int hour = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());

            if (hour >= 0 && hour < 6) return 0;    // 0-5h -> 6h
            if (hour >= 6 && hour < 9) return 1;    // 6-8h -> 9h
            if (hour >= 9 && hour < 12) return 2;   // 9-11h -> 12h
            if (hour >= 12 && hour < 15) return 3;  // 12-14h -> 15h
            if (hour >= 15 && hour < 18) return 4;  // 15-17h -> 18h
            if (hour >= 18 && hour < 21) return 5;  // 18-20h -> 21h
            if (hour >= 21 && hour < 24) return 6;  // 21-23h -> 24h
            return 6; // Default to last slot

        } else if ("week".equals(period)) {
            int dayOfWeek = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());
            switch (dayOfWeek) {
                case 1: return 6; // Sunday -> CN (index 6)
                case 2: return 0; // Monday -> T2 (index 0)
                case 3: return 1; // Tuesday -> T3 (index 1)
                case 4: return 2; // Wednesday -> T4 (index 2)
                case 5: return 3; // Thursday -> T5 (index 3)
                case 6: return 4; // Friday -> T6 (index 4)
                case 7: return 5; // Saturday -> T7 (index 5)
                default: return -1;
            }
        } else if ("month".equals(period)) {
            int day = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());
            return day - 1; // Đúng rồi
        } else if ("year".equals(period)) {
            int month = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());
            return month - 1; // Đúng rồi
        }
        return -1;
    }

}