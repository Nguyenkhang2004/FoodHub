package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.PayOSRequest;
import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.InvoiceResponse;
import com.example.FoodHub.dto.response.PaymentResponse;
import com.example.FoodHub.dto.response.RevenueStatsResponseForCashier;
import com.example.FoodHub.entity.*;
import com.example.FoodHub.enums.OrderStatus;
import com.example.FoodHub.enums.PaymentMethod;
import com.example.FoodHub.enums.PaymentStatus;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.mapper.PaymentMapper;
import com.example.FoodHub.repository.*;
import com.example.FoodHub.utils.PayOSUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RestaurantOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

//của khứa khang
    PaymentMapper paymentMapper;
    PayOSUtils payOSUtils;



    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for order ID: {}", request.getOrderId());

        // Validate order
        RestaurantOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (OrderStatus.CANCELLED.name().equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELED);
        }
        if (!OrderStatus.COMPLETED.name().equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_EXISTED);
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
        return paymentMapper.toPaymentResponse(payment);
    }
// trên này là của khứa khang làm cmm, bố đéo đụng






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


    // Hàm mới: Lấy giao dịch theo khoảng thời gian và trạng thái
    public List<PaymentResponse> getTransactionsByDateAndStatus(Instant start, Instant end, String status) {
        log.info("Fetching transactions from {} to {} with status {}", start, end, status);
        try {
            List<Payment> payments = paymentRepository.findByCreatedAtBetweenAndStatus(start, end, status);
            log.info("Found {} transactions with status {}", payments.size(), status);
            return payments.stream()
                    .map(this::mapToPaymentResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching transactions by date and status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch transactions");
        }
    }




    public List<String> getTransactionSuggestions(Instant start, Instant end, String query) {
        log.info("Fetching transaction suggestions from {} to {} with query {}", start, end, query);
        if (end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        List<Payment> payments = paymentRepository.findByCreatedAtBetween(start, end);
        return payments.stream()
                .filter(p -> (String.valueOf(p.getOrder().getId()).toLowerCase().contains(query != null ? query.toLowerCase() : "")) // Sử dụng order.getId()
                        || (p.getTransactionId() != null && p.getTransactionId().toLowerCase().contains(query != null ? query.toLowerCase() : "")))
                .map(p -> String.valueOf(p.getOrder().getId()) + " - " + (p.getTransactionId() != null ? p.getTransactionId() : "N/A"))
                .distinct()
                .limit(5) // Giới hạn 5 gợi ý
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


    public InvoiceResponse getOrderDetails(Integer orderId) {
        // Lấy thông tin đơn hàng
        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Lấy thông tin thanh toán
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));


        System.out.println("Class of updatedAt: " + payment.getUpdatedAt().getClass());
        System.out.println("Raw updatedAt: " + payment.getUpdatedAt());  //2 cái này để test thôi nhé

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

}