package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.PayOSRequest;
import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.InvoiceResponse;
import com.example.FoodHub.dto.response.PaymentResponse;
import com.example.FoodHub.dto.response.RestaurantOrderResponse;
import com.example.FoodHub.dto.response.RevenueStatsResponseForCashier;
import com.example.FoodHub.entity.*;
import com.example.FoodHub.enums.NotificationType;
import com.example.FoodHub.enums.OrderStatus;
import com.example.FoodHub.enums.PaymentMethod;
import com.example.FoodHub.enums.PaymentStatus;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.PaymentMapper;
import com.example.FoodHub.mapper.RestaurantOrderMapper;
import com.example.FoodHub.mapper.UserMapper;
import com.example.FoodHub.repository.*;
import com.example.FoodHub.utils.PayOSUtils;
import com.example.FoodHub.utils.TimeUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    private final UserMapper userMapper;

    PaymentRepository paymentRepository;
    RestaurantOrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    PaymentMapper paymentMapper;
    PayOSUtils payOSUtils;
    RestaurantOrderMapper restaurantOrderMapper;
    NotificationService notificationService;

    // Process payment for an order

//    @Transactional
//    public PaymentResponse processPayment(@NotNull PaymentRequest request) {
//        log.info("Processing payment for order ID: {}", request.getOrderId());
//
//        RestaurantOrder order = orderRepository.findById(request.getOrderId())
//                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
//
//        if (OrderStatus.CANCELLED.name().equals(order.getStatus())) {
//            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELED, "Order has been canceled");
//        }
//        if (!OrderStatus.PENDING.name().equals(order.getStatus()) && !OrderStatus.CONFIRMED.name().equals(order.getStatus())) {
//            throw new AppException(ErrorCode.ORDER_COMPLETED, "Order cannot be paid in current status: " + order.getStatus());
//        }
//
//        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
//                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND, "Payment record not found for order: " + request.getOrderId()));
//
//        // Tính tổng tiền dựa trên giá từ MenuItem
//        BigDecimal totalAmount = orderItemRepository.findById(request.getOrderId()).stream()
//                .map(item -> {
//                    MenuItem menuItem = item.getMenuItem();
//                    return menuItem != null ? menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())) : BigDecimal.ZERO;
//                })
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
//            throw new AppException(ErrorCode.INVALID_ORDER_AMOUNT, "Invalid order amount");
//        }
//
//        payment.setAmount(totalAmount);
//        payment.setPaymentMethod(request.getPaymentMethod());
//        payment.setStatus(PaymentStatus.PAID.name());
//        payment.setUpdatedAt(Instant.now()); // Cập nhật thời gian thanh toán thực tế
//        paymentRepository.save(payment);
//
//        List<OrderItem> orderItems = orderItemRepository.findByOrderId(request.getOrderId());
//        orderItems.forEach(item -> {
//            item.setStatus(OrderItemStatus.COMPLETED.name()); // Consider using enum if available
//            orderItemRepository.save(item);
//        });
//
//        order.setStatus(OrderStatus.COMPLETED.name());
//        orderRepository.save(order);
//
//        return mapToPaymentResponse(payment);
//    }

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
                    Duration.between(payment.getCreatedAt(), Instant.now()).toMinutes() < 10) {
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
        payment.setCreatedAt(Instant.now());

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
        payment.setUpdatedAt(Instant.now());
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
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);
        if(PaymentStatus.PAID.name().equals(finalStatus)) {
            notificationService.notifyOrderEvent(payment.getOrder(), NotificationType.BANKING_COMPLETED.name());
        }
        return paymentMapper.toPaymentResponse(payment);
    }

    public PaymentResponse getUncompletedPaymentByOrderId(Integer orderId) {
        log.info("Fetching payment for order ID: {}", orderId);
        Payment payment = paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.PENDING.name())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return paymentMapper.toPaymentResponse(payment);
    }
    // Cancel or refund an order
//    @Transactional
//    public void cancelOrRefundOrder(int orderId) {
//        log.info("Canceling or refunding order ID: {}", orderId);
//
//        RestaurantOrder order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
//
//        if (OrderStatus.CANCELLED.name().equals(order.getStatus())) {
//            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELED, "Order has already been canceled");
//        }
//        if (OrderStatus.COMPLETED.name().equals(order.getStatus())) {
//            throw new AppException(ErrorCode.ORDER_COMPLETED, "Cannot cancel completed order");
//        }
//
//        Payment payment = paymentRepository.findByOrderId(orderId)
//                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND, "Payment record not found for order: " + orderId));
//
//        String paymentStatus = payment.getStatus();
//        if (paymentStatus == null) {
//            log.error("Payment status is null for orderId: {}", orderId);
//            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Payment status is null");
//        }
//        if (PaymentStatus.PAID.name().equals(paymentStatus)) {
//            payment.setStatus(PaymentStatus.REFUNDED.name());
//        } else if (PaymentStatus.REFUNDED.name().equals(paymentStatus) || PaymentStatus.CANCELLED.name().equals(paymentStatus)) {
//            throw new AppException(ErrorCode.PAYMENT_ALREADY_PROCESSED, "Payment has already been processed");
//        } else {
//            payment.setStatus(PaymentStatus.CANCELLED.name());
//        }
//        paymentRepository.save(payment);
//
//        orderRepository.save(order);
//
//        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
//        orderItems.forEach(item -> {
//            item.setStatus(OrderItemStatus.CANCELLED.name()); // Consider using enum if available
//            orderItemRepository.save(item);
//        });
//    }

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

    // Get total revenue by date
    public BigDecimal getTotalRevenueByDate(Instant date) {
        log.info("Fetching total revenue for date: {}", date);
        BigDecimal totalRevenue = paymentRepository.calculateTotalRevenueByDate(date);
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    // Get revenue stats by date
    public RevenueStatsResponseForCashier getRevenueStatsByDate(Instant date) {
        log.info("Fetching revenue stats for date: {}", date);
        Instant startOfDay = date.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate()
                .atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        Instant endOfDay = date.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate()
                .plusDays(1).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).minusSeconds(1).toInstant();

        return calculateRevenueStats(startOfDay, endOfDay);
    }

    // Get revenue stats by date range
    public RevenueStatsResponseForCashier getRevenueStatsByDateRange(Instant start, Instant end) {
        log.info("Fetching revenue stats from {} to {}", start, end);
        if (end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        return calculateRevenueStats(start, end);
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

            if (PaymentStatus.PAID.name().equals(payment.getStatus())) {
                totalRevenue = totalRevenue.add(amount);
                paidRevenue = paidRevenue.add(amount);

                if (PaymentMethod.CASH.name().equals(payment.getPaymentMethod())) {
                    cashRevenue = cashRevenue.add(amount);
                } else if (PaymentMethod.BANKING.name().equals(payment.getPaymentMethod())) {
                    vnpayRevenue = vnpayRevenue.add(amount);
                }
            } else if (PaymentStatus.PENDING.name().equals(payment.getStatus())) {
                pendingRevenue = pendingRevenue.add(amount);
            } else if (PaymentStatus.CANCELLED.name().equals(payment.getStatus())) {
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

    // Existing PaymentService methods
    public List<Payment> getPendingPayments() {
        log.info("Fetching pending payments");
        return paymentRepository.findByStatus(PaymentStatus.PENDING.name());
    }

    public Payment refundPayment(Integer paymentId) {
        log.info("Refunding payment with ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        payment.setStatus(PaymentStatus.REFUNDED.name());
        payment.setUpdatedAt(TimeUtils.getNowInVietNam());
        return paymentRepository.save(payment);
    }

    public List<Payment> getAllTransactions() {
        log.info("Fetching all transactions");
        return paymentRepository.findAll();
    }

    // hóa đơn

    // Phương thức mới: Lấy thông tin hóa đơn
    public InvoiceResponse getOrderDetails(Integer orderId) {
        // Lấy thông tin đơn hàng
        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Lấy thông tin thanh toán
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        // Lấy thông tin bàn (truy cập qua quan hệ)
        RestaurantTable table = order.getTable(); // Sử dụng getTable() thay vì findById
        if (table == null) {
            throw new AppException(ErrorCode.TABLE_NOT_EXISTED);
        }

        // Lấy thông tin khách hàng (truy cập qua quan hệ)
        User user = order.getUser(); // Sử dụng getUser() thay vì findById
        if (user == null) {
            throw new AppException(ErrorCode.TABLE_NOT_EXISTED);
        }

        // Lấy danh sách món ăn
        // Lấy danh sách món ăn
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        List<Map<String, Object>> orderItems = items.stream().map(item -> {
            MenuItem menuItem = item.getMenuItem(); // vẫn lấy để lấy tên món

            if (menuItem == null) {
                throw new AppException(ErrorCode.MENU_ITEM_NOT_FOUND);
            }

            BigDecimal unitPrice = menuItem.getPrice(); // ✅ GIÁ GỐC TỪ MENU
            int quantity = item.getQuantity();
            BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("itemName", menuItem.getName());
            itemMap.put("quantity", quantity);
            itemMap.put("price", unitPrice);
            itemMap.put("total", total); // nếu bạn muốn hiện tổng tiền từng món
            return itemMap;
        }).collect(Collectors.toList());


        // Tạo response
        InvoiceResponse response = new InvoiceResponse();
        response.setOrderId(order.getId());
//        response.setPaymentDate(payment.getUpdatedAt());
        response.setTableNumber(table.getTableNumber());
        response.setCustomerName(user.getUsername());
        response.setCustomerEmail(user.getEmail());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod().toString());
        response.setStatus(payment.getStatus().toString());
        response.setTransactionId(payment.getTransactionId());
        response.setOrderItems(orderItems);

        return response;
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
    public Page<PaymentResponse> getPayments(
            String period, Instant startDate, Instant endDate, String status, String transactionId, Pageable pageable) {
        Instant start = getStartDate(period, startDate);
        Instant end = getEndDate(period, endDate);

        // Xử lý transactionId rỗng
        if (transactionId != null && transactionId.trim().isEmpty()) {
            transactionId = null;
        }

        log.info("Fetching payments with period: {}, start: {}, end: {}, status: {}, transactionId: {}",
                period, start, end, status, transactionId);

        Page<Payment> payments = paymentRepository.findByStatusAndCreatedAtBetween(start, end, status, transactionId, pageable);
        log.info("Found {} payments", payments.getTotalElements());
        return payments.map(paymentMapper::toPaymentResponse);
    }

    // Phương thức: Xem chi tiết giao dịch
    public RestaurantOrderResponse getPaymentDetails(String transactionId) {
        log.info("Fetching payment details for transactionId: {}", transactionId);
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch: " + transactionId));
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
}

