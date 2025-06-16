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
import java.time.ZonedDateTime;
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

        log.info("System default time zone: {}", ZoneId.systemDefault());
        log.info("Using time zone: {}", zoneId);
        log.info("Current date: {}", today);

        // Sử dụng UTC cho toàn bộ hệ thống
        ZoneId utcZone = ZoneId.of("UTC");

        switch (period) {
            case "today":
                start = today.atStartOfDay(utcZone).toInstant();
                end = today.plusDays(1).atStartOfDay(utcZone).toInstant();
                prevStart = today.minusDays(1).atStartOfDay(utcZone).toInstant();
                prevEnd = today.atStartOfDay(utcZone).toInstant();
                dailyLabels = List.of("6h", "9h", "12h", "15h", "18h", "21h", "24h");
                log.info("Today period (UTC): start={}, end={}, prevStart={}, prevEnd={}", start, end, prevStart, prevEnd);
                break;
            case "week":
                start = today.minusDays(6).atStartOfDay(utcZone).toInstant();
                end = today.plusDays(1).atStartOfDay(utcZone).toInstant();
                prevStart = today.minusDays(13).atStartOfDay(utcZone).toInstant();
                prevEnd = today.minusDays(6).atStartOfDay(utcZone).toInstant();
                dailyLabels = List.of("T2", "T3", "T4", "T5", "T6", "T7", "CN");
                log.info("Week period (UTC): start={}, end={}, prevStart={}, prevEnd={}", start, end, prevStart, prevEnd);
                break;
            case "month":
                start = today.withDayOfMonth(1).atStartOfDay(utcZone).toInstant();
                end = today.plusDays(1).atStartOfDay(utcZone).toInstant();
                prevStart = today.minusMonths(1).withDayOfMonth(1).atStartOfDay(utcZone).toInstant();
                prevEnd = today.withDayOfMonth(1).atStartOfDay(utcZone).toInstant();
                dailyLabels = generateMonthLabels(today);
                log.info("Month period (UTC): start={}, end={}, prevStart={}, prevEnd={}", start, end, prevStart, prevEnd);
                break;
            case "year":
                start = today.withDayOfYear(1).atStartOfDay(utcZone).toInstant();
                end = today.plusDays(1).atStartOfDay(utcZone).toInstant();
                prevStart = today.minusYears(1).withDayOfYear(1).atStartOfDay(utcZone).toInstant();
                prevEnd = today.withDayOfYear(1).atStartOfDay(utcZone).toInstant();
                dailyLabels = List.of("Th1", "Th2", "Th3", "Th4", "Th5", "Th6", "Th7", "Th8", "Th9", "Th10", "Th11", "Th12");
                log.info("Year period (UTC): start={}, end={}, prevStart={}, prevEnd={}", start, end, prevStart, prevEnd);
                break;
            case "specific":
                if (specificDate == null) {
                    throw new IllegalArgumentException("Specific date is required for period 'specific'");
                }
                // Sử dụng UTC thay vì Asia/Ho_Chi_Minh
                LocalDate specific = LocalDate.parse(specificDate);
                start = specific.atStartOfDay(utcZone).toInstant();
                end = specific.plusDays(1).atStartOfDay(utcZone).toInstant();
                prevStart = specific.minusDays(1).atStartOfDay(utcZone).toInstant();
                prevEnd = specific.atStartOfDay(utcZone).toInstant();
                dailyLabels = List.of("6h", "8h", "10h", "12h", "14h", "16h", "18h", "20h", "22h", "24h");

                log.info("Specific period (UTC): date={}, start={}, end={}, prevStart={}, prevEnd={}",
                        specificDate, start, end, prevStart, prevEnd);
                break;
            default:
                throw new IllegalArgumentException("Invalid period: " + period);
        }

        // Tính tổng doanh thu và số đơn hàng
        BigDecimal revenue = orderRepository.findTotalRevenueByPeriod(start, end)
                .orElse(BigDecimal.ZERO);
        Long orders = orderRepository.countOrdersByPeriod(start, end);
        BigDecimal previousRevenue = orderRepository.findTotalRevenueByPeriod(prevStart, prevEnd)
                .orElse(BigDecimal.ZERO);

        log.info("Revenue: {}, Orders: {}, Previous Revenue: {}", revenue, orders, previousRevenue);

        // Tính doanh thu theo ngày/giờ
        List<Object[]> dailyRevenueData = getDailyRevenueData(period, start, end);
        List<BigDecimal> dailyRevenue = new ArrayList<>(Collections.nCopies(dailyLabels.size(), BigDecimal.ZERO));

        log.info("Processing {} daily revenue records for period: {}", dailyRevenueData.size(), period);

        for (Object[] data : dailyRevenueData) {
            try {
                int index = getIndexForDailyLabel(data[0], period, ZonedDateTime.ofInstant(start, ZoneId.of("UTC")));
                if (index >= 0 && index < dailyLabels.size()) {
                    dailyRevenue.set(index, (BigDecimal) data[1]);
                    log.info("Daily revenue mapping: rawData={}, index={}, label={}, value={}",
                            data[0], index, dailyLabels.get(index), data[1]);
                } else {
                    log.warn("Invalid index {} for rawData={}, period={}", index, data[0], period);
                }
            } catch (Exception e) {
                log.warn("Error processing daily revenue data: {}, period: {}", data[0], period, e);
            }
        }

        // Tính doanh thu theo phương thức thanh toán
        List<Object[]> paymentMethodData = orderRepository.findRevenueByPaymentMethod(start, end);
        List<String> paymentMethodLabels = new ArrayList<>();
        List<BigDecimal> paymentMethodRevenue = new ArrayList<>();
        for (Object[] data : paymentMethodData) {
            paymentMethodLabels.add((String) data[0]);
            paymentMethodRevenue.add((BigDecimal) data[1]);
            log.info("Payment method: {}, amount: {}", data[0], data[1]);
        }

        // Tính doanh thu theo loại đơn hàng
        List<Object[]> orderTypeData = orderRepository.findRevenueByOrderType(start, end);
        List<String> orderTypeLabels = new ArrayList<>();
        List<BigDecimal> orderTypeRevenue = new ArrayList<>();
        for (Object[] data : orderTypeData) {
            orderTypeLabels.add((String) data[0]);
            orderTypeRevenue.add((BigDecimal) data[1]);
            log.info("Order type: {}, amount: {}", data[0], data[1]);
        }

        // Tính top món ăn
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
        if (revenue.compareTo(BigDecimal.ZERO) == 0 && orders == 0 && topDishes.isEmpty()) {
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

    private int getIndexForDailyLabel(Object timeData, String period, ZonedDateTime start) {
        if ("today".equals(period) || "specific".equals(period)) {
            int hour = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());
            if (hour <= 6) return 0;
            if (hour <= 8) return 1;
            if (hour <= 10) return 2;
            if (hour <= 12) return 3;
            if (hour <= 14) return 4;
            if (hour <= 16) return 5;
            if (hour <= 18) return 6;
            if (hour <= 20) return 7;
            if (hour <= 22) return 8;
            return 9;
        } else if ("week".equals(period)) {
            // MySQL DAYOFWEEK(): 1=Sunday, 2=Monday, 3=Tuesday, 4=Wednesday, 5=Thursday, 6=Friday, 7=Saturday
            // dailyLabels: ["T2", "T3", "T4", "T5", "T6", "T7", "CN"] (Monday to Sunday)
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
            return day - 1;
        } else if ("year".equals(period)) {
            int month = timeData instanceof Integer ? (Integer) timeData : Integer.parseInt(timeData.toString());
            return month - 1;
        }
        return -1;
    }
}