package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.PayOSRequest;
import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.*;
import com.example.FoodHub.entity.*;
import com.example.FoodHub.enums.*;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.PaymentMapper;
import com.example.FoodHub.mapper.RestaurantOrderMapper;
import com.example.FoodHub.repository.*;
import com.example.FoodHub.utils.PayOSUtils;
import com.example.FoodHub.utils.TimeUtils;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {

    PaymentRepository paymentRepository;
    RestaurantOrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    NotificationService notificationService;
    PaymentMapper paymentMapper;
    PayOSUtils payOSUtils;
    RestaurantOrderMapper restaurantOrderMapper;
    ScanQRService scanQRService;

    @PreAuthorize("hasAuthority('PROCESS_PAYMENT')")
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for order ID: {}", request.getOrderId());

        // Validate order
        RestaurantOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (OrderStatus.CANCELLED.name().equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELED);
        }

        // Check existing payment
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElse(null);
        if (payment != null) {
            if (PaymentStatus.PAID.name().equals(payment.getStatus())) {
                throw new AppException(ErrorCode.PAYMENT_ALREADY_PAID);
            }
            if (PaymentStatus.PENDING.name().equals(payment.getStatus()) &&
                    request.getPaymentMethod().equals(PaymentMethod.BANKING.name()) &&
                    payment.getCreatedAt() != null &&
                    Duration.between(payment.getCreatedAt(), TimeUtils.getNowInVietNam()).toMinutes() < 10) {
                // Re-use existing paymentUrl if still within 10-minute window
                log.info("Re-using existing payment for order ID: {}", request.getOrderId());
                return paymentMapper.toPaymentResponse(payment);
            }
            if (PaymentStatus.PENDING.name().equals(payment.getStatus())) {
                log.info("Existing payment ID: {} is pending but expired, awaiting scheduler cancellation", payment.getId());
                throw new AppException(ErrorCode.PAYMENT_EXPIRED);
            }
        }

        // Create new payment entity
        payment = paymentMapper.toPayment(request);
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(request.getPaymentMethod().equals(PaymentMethod.BANKING.name())
                ? PaymentStatus.PENDING.name()
                : PaymentStatus.UNPAID.name());
        payment.setCreatedAt(TimeUtils.getNowInVietNam());

        if (request.getPaymentMethod().equals(PaymentMethod.BANKING.name())) {
            String paymentUrl = payOSUtils.generatePaymentUrl(payment);
            payment.setPaymentUrl(paymentUrl);
            String transactionId = payOSUtils.getTransactionId(paymentUrl);
            payment.setTransactionId(transactionId);
            payment.setPaymentUrl(paymentUrl);
        }

        paymentRepository.save(payment);
        return paymentMapper.toPaymentResponse(payment);
    }


    public PaymentResponse updatePaymentStatus(Integer orderId, String newStatus) {
        log.info("Updating payment status for payment with order ID: {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        payment.setStatus(newStatus);
        payment.setUpdatedAt(TimeUtils.getNowInVietNam());
        paymentRepository.save(payment);
        return paymentMapper.toPaymentResponse(payment);
    }

    public PaymentResponse updatePayOSPaymentStatus(PayOSRequest request){
        log.info("PayOS callback received: paymentId={}, status={}, cancel={}, id={}, code={}",
                request.getOrderId(), request.getStatus(), request.isCancel(), request.getId(), request.getCode());
        String finalStatus = request.getStatus();
        if (!"00".equals(request.getCode())) {
            finalStatus = PaymentStatus.FAILED.name(); // PayOS báo lỗi
        }
        Payment payment = paymentRepository.findByOrderId(request.getOrderCode())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        payment.setStatus(finalStatus);
        payment.setUpdatedAt(TimeUtils.getNowInVietNam());
        paymentRepository.save(payment);
        RestaurantOrder order = payment.getOrder();

        if(PaymentStatus.PAID.name().equals(finalStatus) &&
                (!(order.getOrderType().equals(OrderType.DELIVERY.name())
                        || order.getOrderType().equals(OrderType.TAKEAWAY.name())))) {
            order.setStatus(OrderStatus.COMPLETED.name());
            order.setUpdatedAt(TimeUtils.getNowInVietNam());

            orderRepository.save(order);
            RestaurantTable table = order.getTable();
            if(table.getCurrentToken() != null) {
                // Invalidate the current token if it exists
                scanQRService.finishSession(table.getCurrentToken());
            }
        }
        if(PaymentStatus.PAID.name().equals(finalStatus)) {
            notificationService.notifyOrderEvent(payment.getOrder(), NotificationType.BANKING_COMPLETED.name());
        }
        return paymentMapper.toPaymentResponse(payment);
    }

    public PaymentResponse getPaymentStatus(Integer orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND, "Payment not found for order ID: " + orderId));
        PaymentResponse response = new PaymentResponse();
        response.setOrderId(orderId);
        response.setAmount(payment.getAmount());
        response.setTransactionId(payment.getTransactionId());
        response.setStatus(payment.getStatus().toString());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }

    public PaymentResponse getUncompletedPaymentByOrderId(Integer orderId) {
        log.info("Fetching payment for order ID: {}", orderId);
        Payment payment = paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.PENDING.name())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return paymentMapper.toPaymentResponse(payment);
    }

    public Page<PaymentResponse> getPayments(
            String period, Instant startDate, Instant endDate, String status, String transactionId,
            String paymentMethod, BigDecimal minPrice, BigDecimal maxPrice, // THÊM 2 PARAMETER
            Pageable pageable) {

        Instant start = getStartDate(period, startDate);
        Instant end = getEndDate(period, endDate);

        // Xử lý transactionId rỗng
        if (transactionId != null && transactionId.trim().isEmpty()) {
            transactionId = null;
        }

        // Xử lý paymentMethod rỗng
        if (paymentMethod != null && paymentMethod.trim().isEmpty()) {
            paymentMethod = null;
        }

        log.info("Fetching payments with period: {}, start: {}, end: {}, status: {}, transactionId: {}, paymentMethod: {}, minPrice: {}, maxPrice: {}",
                period, start, end, status, transactionId, paymentMethod, minPrice, maxPrice);

        Page<Payment> payments = paymentRepository.findByStatusAndCreatedAtBetween(
                start, end, status, transactionId, paymentMethod, minPrice, maxPrice, // THÊM 2 PARAMETER
                pageable);

        log.info("Found {} payments", payments.getTotalElements());
        return payments.map(paymentMapper::toPaymentResponse);
    }

    // Phương thức: Xem chi tiết giao dịch
    public RestaurantOrderResponse getPaymentDetails(Integer id) {
        log.info("Fetching payment details for id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch: " + id));
        return restaurantOrderMapper.toRestaurantOrderResponse(payment.getOrder());
    }

    private Instant getStartDate(String period, Instant startDate) {
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        ZoneId utcZone = ZoneId.of("UTC");
        LocalDate today = LocalDate.now(zoneId);

        if ("specific".equals(period) && startDate != null) {
            LocalDateTime localDateTime = startDate.atZone(zoneId).toLocalDateTime();
            Instant adjustedStart = localDateTime.toLocalDate().atStartOfDay(utcZone).toInstant();
            log.info("Specific period - Received startDate: {}, Adjusted startDate: {}", startDate, adjustedStart);
            return adjustedStart;
        }

        switch (period != null ? period.toLowerCase() : "today") {
            case "today":
                Instant startToday = today.atStartOfDay(utcZone).toInstant();
                log.info("Today period - Start: {}", startToday);
                return startToday;
            case "week":
                LocalDate mondayOfCurrentWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                Instant startWeek = mondayOfCurrentWeek.atStartOfDay(utcZone).toInstant();
                log.info("Week period - Start: {}", startWeek);
                return startWeek;
            case "month":
                Instant startMonth = today.withDayOfMonth(1).atStartOfDay(utcZone).toInstant();
                log.info("Month period - Start: {}", startMonth);
                return startMonth;
            case "year":
                Instant startYear = today.withDayOfYear(1).atStartOfDay(utcZone).toInstant();
                log.info("Year period - Start: {}", startYear);
                return startYear;
            default:
                Instant defaultStart = today.minusDays(30).atStartOfDay(utcZone).toInstant();
                log.info("Default period - Start: {}", defaultStart);
                return defaultStart;
        }
    }

    private Instant getEndDate(String period, Instant endDate) {
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        ZoneId utcZone = ZoneId.of("UTC");
        LocalDate today = LocalDate.now(zoneId);

        if ("specific".equals(period) && endDate != null) {
            LocalDateTime localDateTime = endDate.atZone(zoneId).toLocalDateTime();
            Instant adjustedStart = localDateTime.toLocalDate().atStartOfDay(utcZone).toInstant();
            log.info("Specific period - Received enddate: {}, Adjusted enddate: {}", endDate, adjustedStart);
            return adjustedStart;
        }
        switch (period != null ? period.toLowerCase() : "today") {
            case "today":
                Instant endToday = today.plusDays(1).atStartOfDay(utcZone).toInstant();
                log.info("Today period - End: {}", endToday);
                return endToday;
            case "week":
                LocalDate sundayOfCurrentWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
                Instant endWeek = sundayOfCurrentWeek.plusDays(1).atStartOfDay(utcZone).toInstant();
                log.info("Week period - End: {}", endWeek);
                return endWeek;
            case "month":
                Instant endMonth = today.withDayOfMonth(today.lengthOfMonth()).plusDays(1).atStartOfDay(utcZone).toInstant();
                log.info("Month period - End: {}", endMonth);
                return endMonth;
            case "year":
                Instant endYear = today.withDayOfYear(today.lengthOfYear()).plusDays(1).atStartOfDay(utcZone).toInstant();
                log.info("Year period - End: {}", endYear);
                return endYear;
            default:
                Instant defaultEnd = today.plusDays(1).atStartOfDay(utcZone).toInstant();
                log.info("Default period - End: {}", defaultEnd);
                return defaultEnd;
        }
    }


//========================================================================================
//========================================================================================
//biên giới
//========================================================================================
//========================================================================================



//========================================================================================

    // Process payment for an order

    @Transactional
    public PaymentResponse processPayment(@NotNull PaymentRequest request) {
        log.info("Processing payment for order ID: {}", request.getOrderId());

        RestaurantOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if ("CANCELLED".equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELED, "Order has been canceled");
        }
        if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_COMPLETED, "Order cannot be paid in current status: " + order.getStatus());
        }

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND, "Payment record not found for order: " + request.getOrderId()));

        // Tính tổng tiền dựa trên giá từ MenuItem
        BigDecimal totalAmount = orderItemRepository.findByOrderId(request.getOrderId()).stream()
                .map(item -> {
                    MenuItem menuItem = item.getMenuItem();
                    return menuItem != null ? menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())) : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_ORDER_AMOUNT, "Invalid order amount");
        }

        payment.setAmount(totalAmount);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("PAID");
        payment.setUpdatedAt(Instant.now()); // Cập nhật thời gian thanh toán thực tế
        paymentRepository.save(payment);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(request.getOrderId());
        orderItems.forEach(item -> {
            item.setStatus("COMPLETED");
            orderItemRepository.save(item);
        });

        order.setStatus("COMPLETED");
        orderRepository.save(order);

        return mapToPaymentResponse(payment);
    }
//========================================================================================


    // Cancel or refund an order
    @Transactional
    public void cancelOrRefundOrder(int orderId) {
        log.info("Canceling or refunding order ID: {}", orderId);

        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if ("CANCELLED".equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELED, "Order has already been canceled");
        }
        if ("COMPLETED".equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_COMPLETED, "Cannot cancel completed order");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND, "Payment record not found for order: " + orderId));

        String paymentStatus = payment.getStatus();
        if (paymentStatus == null) {
            log.error("Payment status is null for orderId: {}", orderId);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Payment status is null");
        }
        if ("PAID".equals(paymentStatus)) {
            payment.setStatus("REFUNDED");
        } else if ("REFUNDED".equals(paymentStatus) || "CANCELLED".equals(paymentStatus)) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PROCESSED, "Payment has already been processed");
        } else {
            payment.setStatus("CANCELLED");
        }
        paymentRepository.save(payment);

        order.setStatus("CANCELLED");
        orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        orderItems.forEach(item -> {
            item.setStatus("CANCELLED");
            orderItemRepository.save(item);
        });
    }
//========================================================================================

    // Get transactions by date range
    public List<PaymentResponse> getTransactionsByDate(Instant start, Instant end) {
        log.info("Fetching transactions from {} to {}", start, end);
        if (end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        List<Payment> payments = paymentRepository.findByCreatedAtBetween(start, end);
        return payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

//========================================================================================





    @Transactional(readOnly = true)
    public ApiResponse<?> searchTransactions(String query) {
        try {
            log.info("Searching transactions with query: {}", query);

            // Tính ngày hôm nay theo múi giờ Asia/Ho_Chi_Minh (UTC+7)
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            Instant startOfDay = now.toLocalDate().atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
            Instant endOfDay = now.toLocalDate().plusDays(1).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).minusSeconds(1).toInstant();

            List<Payment> payments;
            if (query.matches("\\d+")) {
                Integer orderId = Integer.parseInt(query);
                payments = paymentRepository.findByCreatedAtBetween(startOfDay, endOfDay)
                        .stream()
                        .filter(p -> p.getOrder() != null && p.getOrder().getId().equals(orderId))
                        .collect(Collectors.toList());
                log.info("Search by orderId: {}, Found: {}", orderId, payments);
            } else {
                payments = paymentRepository.findByTransactionIdContainingAndCreatedAtBetween(query, startOfDay, endOfDay);
                log.info("Search by transactionId containing: {}, Found: {}", query, payments);
            }

            List<PaymentResponse> result = payments.stream()
                    .map(payment -> {
                        PaymentResponse response = paymentMapper.toPaymentResponse(payment);
                        if (response.getCreatedAt() != null) {
                            // Giữ nguyên logic trừ 7 tiếng từ UTC sang UTC-7
                            ZonedDateTime zonedDateTime = response.getCreatedAt().atZone(ZoneId.of("UTC")).minusHours(7);
                            response.setCreatedAt(zonedDateTime.toInstant()); // Giữ dạng Instant nhưng đã trừ 7 tiếng
                        }
                        if (response.getUpdatedAt() != null) {
                            // Giữ nguyên logic trừ 7 tiếng từ UTC sang UTC-7
                            ZonedDateTime zonedDateTime = response.getUpdatedAt().atZone(ZoneId.of("UTC")).minusHours(7);
                            response.setUpdatedAt(zonedDateTime.toInstant()); // Giữ dạng Instant nhưng đã trừ 7 tiếng
                        }
                        return response;
                    })
                    .collect(Collectors.toList());
            log.info("Mapped search result: {}", result);

            return ApiResponse.builder()
                    .code(1000)
                    .message("Success")
                    .result(result)
                    .build();
        } catch (Exception e) {
            log.error("Error searching transactions: {}", e.getMessage(), e);
            return ApiResponse.builder()
                    .code(9999)
                    .message("Error: " + e.getMessage())
                    .result(List.of())
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<?> getSuggestions(String query) {
        try {
            log.info("Fetching suggestions with query: {}", query);

            // Tính ngày hôm nay theo múi giờ Asia/Ho_Chi_Minh (UTC+7)
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            Instant startOfDay = now.toLocalDate().atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
            Instant endOfDay = now.toLocalDate().plusDays(1).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).minusSeconds(1).toInstant();

            List<Payment> orderIdSuggestions = paymentRepository.findByCreatedAtBetween(startOfDay, endOfDay)
                    .stream()
                    .filter(p -> p.getOrder() != null && String.valueOf(p.getOrder().getId()).toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            List<Payment> transactionIdSuggestions = paymentRepository.findByCreatedAtBetween(startOfDay, endOfDay)
                    .stream()
                    .filter(p -> p.getTransactionId() != null && p.getTransactionId().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());

            List<String> suggestions = Stream.concat(orderIdSuggestions.stream(), transactionIdSuggestions.stream())
                    .map(p -> {
                        PaymentResponse response = paymentMapper.toPaymentResponse(p);
                        if (response.getCreatedAt() != null) {
                            // Giữ nguyên logic trừ 7 tiếng từ UTC sang UTC-7
                            ZonedDateTime zonedDateTime = response.getCreatedAt().atZone(ZoneId.of("UTC")).minusHours(7);
                            response.setCreatedAt(zonedDateTime.toInstant());
                        }
                        if (response.getUpdatedAt() != null) {
                            // Giữ nguyên logic trừ 7 tiếng từ UTC sang UTC-7
                            ZonedDateTime zonedDateTime = response.getUpdatedAt().atZone(ZoneId.of("UTC")).minusHours(7);
                            response.setUpdatedAt(zonedDateTime.toInstant());
                        }
                        return String.valueOf(p.getOrder().getId()) + " - " + (p.getTransactionId() != null ? p.getTransactionId() : "N/A");
                    })
                    .distinct()
                    .limit(5)
                    .collect(Collectors.toList());
            log.info("Suggestions result: {}", suggestions);

            return ApiResponse.builder()
                    .code(1000)
                    .message("Success")
                    .result(suggestions)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching suggestions: {}", e.getMessage(), e);
            return ApiResponse.builder()
                    .code(9999)
                    .message("Error: " + e.getMessage())
                    .result(List.of())
                    .build();
        }
    }
//========================================================================================

//======================

    // Get revenue stats by date
    public RevenueStatsResponseForCashier getRevenueStatsByDate(Instant date) {
        log.info("Fetching revenue stats for date: {}", date);
        Instant startOfDay = date.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate()
                .atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        Instant endOfDay = date.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate()
                .plusDays(1).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).minusSeconds(1).toInstant();

        return calculateRevenueStats(startOfDay, endOfDay);
    }


    // Calculate revenue stats
    private RevenueStatsResponseForCashier calculateRevenueStats(Instant start, Instant end) {
        List<Payment> payments = paymentRepository.findByCreatedAtBetween(start, end);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal cashRevenue = BigDecimal.ZERO;
        BigDecimal vnpayRevenue = BigDecimal.ZERO;
        BigDecimal pendingRevenue = BigDecimal.ZERO;
        BigDecimal paidRevenue = BigDecimal.ZERO;
        BigDecimal cancelledRevenue = BigDecimal.ZERO;

        for (Payment payment : payments) {
            BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;

            if ("PAID".equals(payment.getStatus())) {
                totalRevenue = totalRevenue.add(amount);
                paidRevenue = paidRevenue.add(amount);

                if ("CASH".equals(payment.getPaymentMethod())) {
                    cashRevenue = cashRevenue.add(amount);
                } else if ("VNPAY".equals(payment.getPaymentMethod())) {
                    vnpayRevenue = vnpayRevenue.add(amount);
                }
            } else if ("PENDING".equals(payment.getStatus())) {
                pendingRevenue = pendingRevenue.add(amount);
            } else if ("CANCELLED".equals(payment.getStatus())) {
                cancelledRevenue = cancelledRevenue.add(amount);
            }
        }

        return new RevenueStatsResponseForCashier(totalRevenue, cashRevenue, vnpayRevenue,
                pendingRevenue, paidRevenue, cancelledRevenue);
    }

    // Map Payment to PaymentResponse
    private PaymentResponse mapToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setOrderId(payment.getOrder().getId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setTransactionId(payment.getTransactionId());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }
//========================================================================================
//========================================================================================


    public InvoiceResponse getOrderDetails(Integer orderId) {
        // Lấy thông tin đơn hàng
        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Lấy thông tin thanh toán
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        // Lấy thông tin bàn
        RestaurantTable table = order.getTable();
        if (table == null) {
            throw new AppException(ErrorCode.TABLE_NOT_EXISTED);
        }

        // Lấy thông tin khách hàng
        User user = order.getUser();
        if (user == null) {
            throw new AppException(ErrorCode.TABLE_NOT_EXISTED);
        }

        // Lấy danh sách món ăn
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        List<Map<String, Object>> orderItems = items.stream().map(item -> {
            MenuItem menuItem = item.getMenuItem();
            if (menuItem == null) {
                throw new AppException(ErrorCode.MENU_ITEM_NOT_FOUND);
            }
            BigDecimal unitPrice = menuItem.getPrice();
            int quantity = item.getQuantity();
            BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("itemName", menuItem.getName());
            itemMap.put("quantity", quantity);
            itemMap.put("price", unitPrice);
            itemMap.put("total", total);
            return itemMap;
        }).collect(Collectors.toList());

        // Tạo response
        InvoiceResponse response = new InvoiceResponse();
        response.setOrderId(order.getId());
        response.setPaymentDate(payment.getUpdatedAt());
        response.setTableNumber(table.getTableNumber());
        response.setCustomerName(user.getUsername());
        response.setCustomerEmail(user.getEmail());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod().toString());
        response.setStatus(payment.getStatus().toString());
        response.setTransactionId(payment.getTransactionId());
        response.setOrderItems(orderItems);

        // Định dạng paymentDate thành formattedPaymentDate
        String formattedPaymentDate = payment.getUpdatedAt() != null
                ? DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.of("UTC"))
                .format(payment.getUpdatedAt())
                : "N/A";

        response.setFormattedPaymentDate(formattedPaymentDate);

        return response;
    }


//========================================================================================

    @Transactional(readOnly = true)
    public List<PaymentResponse> getNewOrders() {
        try {
            List<Payment> payments = paymentRepository.findByStatus("PENDING");



            return payments.stream()
                    .map(paymentMapper::toPaymentResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to fetch pending orders");
        }
    }

//========================================================================================


    public String generateInvoicePdf(Integer orderId) {

        // Lấy dữ liệu hóa đơn
        InvoiceResponse invoice = getOrderDetails(orderId);


        // Tạo PDF với khổ giấy nhỏ (80mm x 150mm)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            pdf.setDefaultPageSize(new PageSize(226, 425)); // 80mm x 150mm
            pdf.getDocumentInfo().setTitle("Invoice - FoodHub");
            document.setMargins(2, 2, 2, 2); // Giảm lề tối đa để vừa khít

            // Màu cam chủ đạo
            DeviceRgb ORANGE_COLOR = new DeviceRgb(255, 98, 0); // #FF6200

            // Tạo một bảng duy nhất cho toàn bộ nội dung
            float[] columnWidths = {226};
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setBorder(null);

            // Tiêu đề nhà hàng
            table.addCell(new Cell().add(new Paragraph(new Text("FoodHub Restaurant").setFontSize(10).setBold().setFontColor(ORANGE_COLOR))
                    .setTextAlignment(TextAlignment.CENTER)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph("68 Nguyen Huu Tho, Da Nang City")
                    .setFontSize(6)
                    .setTextAlignment(TextAlignment.CENTER)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph("Hotline: 1900-1234")
                    .setFontSize(6)
                    .setTextAlignment(TextAlignment.CENTER)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph("Email: contact@foodhub.vn")
                    .setFontSize(6)
                    .setTextAlignment(TextAlignment.CENTER)).setBorder(null));

            // Tiêu đề hóa đơn
            table.addCell(new Cell().add(new Paragraph(new Text("INVOICE").setFontSize(8).setBold().setFontColor(ORANGE_COLOR))
                    .setTextAlignment(TextAlignment.CENTER)).setBorder(null));

            // Thông tin hóa đơn
            String paymentTime = invoice.getFormattedPaymentDate() != null ? invoice.getFormattedPaymentDate() : "N/A";
            table.addCell(new Cell().add(new Paragraph("Invoice No: " + invoice.getOrderId()).setFontSize(6)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph("Date: " + paymentTime).setFontSize(6)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph("Table: " + (invoice.getTableNumber() != null ? invoice.getTableNumber() : "N/A")).setFontSize(6)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph("Customer: " + (invoice.getCustomerName() != null ? invoice.getCustomerName() : "N/A")).setFontSize(6)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph("Email: " + (invoice.getCustomerEmail() != null ? invoice.getCustomerEmail() : "N/A")).setFontSize(6)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph("Payment Method: " + (invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "N/A")).setFontSize(6)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph("Status: " + (invoice.getStatus() != null ? invoice.getStatus() : "N/A")).setFontSize(6)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph("Transaction ID: " + (invoice.getTransactionId() != null ? invoice.getTransactionId() : "N/A")).setFontSize(6)).setBorder(null));

            // Bảng chi tiết món ăn
            float[] itemColumnWidths = {110, 25, 45, 46};
            Table itemTable = new Table(UnitValue.createPointArray(itemColumnWidths));
            itemTable.setWidth(UnitValue.createPercentValue(100));
            itemTable.setBorder(new SolidBorder(ORANGE_COLOR, 0.5f));

            // Tiêu đề chi tiết
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Item").setBold().setFontColor(ColorConstants.WHITE).setFontSize(5))
                    .setBackgroundColor(ORANGE_COLOR));
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Qty").setBold().setFontColor(ColorConstants.WHITE).setFontSize(5))
                    .setBackgroundColor(ORANGE_COLOR));
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Price").setBold().setFontColor(ColorConstants.WHITE).setFontSize(5))
                    .setBackgroundColor(ORANGE_COLOR));
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Total").setBold().setFontColor(ColorConstants.WHITE).setFontSize(5))
                    .setBackgroundColor(ORANGE_COLOR));

            // Dữ liệu món ăn
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<Map<String, Object>> orderItems = invoice.getOrderItems();
            for (Map<String, Object> item : orderItems) {
                String itemName = (String) item.get("itemName");
                Integer quantity = (Integer) item.get("quantity");
                BigDecimal price = (BigDecimal) item.get("price");
                BigDecimal total = price.multiply(new BigDecimal(quantity != null ? quantity : 0));

                itemName = itemName != null ? itemName.replaceAll("[_\\-]", "") : "N/A";

                itemTable.addCell(new Cell().add(new Paragraph(itemName).setFontSize(5)));
                itemTable.addCell(new Cell().add(new Paragraph(quantity != null ? quantity.toString() : "0").setFontSize(5)));
                itemTable.addCell(new Cell().add(new Paragraph(price != null ? price.toString() : "0").setFontSize(5)));
                itemTable.addCell(new Cell().add(new Paragraph(total.toString()).setFontSize(5)));

                totalAmount = totalAmount.add(total);
            }

            // Hàng tổng cộng
            itemTable.addCell(new Cell(1, 3).add(new Paragraph("Total").setBold().setFontSize(5).setTextAlignment(TextAlignment.RIGHT)));
            itemTable.addCell(new Cell().add(new Paragraph(totalAmount.toString() + " VND").setBold().setFontSize(5)));
            table.addCell(new Cell().add(itemTable));

            // Chân trang
            table.addCell(new Cell().add(new Paragraph("Thank you for choosing FoodHub!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(6)).setBorder(null));
            table.addCell(new Cell().add(new Paragraph("Please check your invoice before leaving.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(5)).setBorder(null));

            document.add(table);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to generate PDF: " + e.getMessage());
        }

        // Lưu file và trả về URL
        String fileName = "invoice_" + orderId + "_" + Instant.now().toString().replace(":", "-") + ".pdf";
        String uploadDir = "invoices/";
        try {
            Files.createDirectories(Paths.get(uploadDir));
            Files.write(Paths.get(uploadDir + fileName), baos.toByteArray());
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to save PDF: " + e.getMessage());
        }

        String baseUrl = "http://localhost:8080"; // Thay bằng domain thực tế
        return baseUrl + "/" + uploadDir + fileName;
    }

//========================================================================================


}