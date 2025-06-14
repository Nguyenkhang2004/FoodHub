package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.InvoiceResponse;
import com.example.FoodHub.dto.response.PaymentResponse;
import com.example.FoodHub.dto.response.RevenueStatsResponseForCashier;
import com.example.FoodHub.entity.*;
import com.example.FoodHub.enums.OrderItemStatus;
import com.example.FoodHub.enums.OrderStatus;
import com.example.FoodHub.enums.PaymentMethod;
import com.example.FoodHub.enums.PaymentStatus;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
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
    private final RestaurantOrderRepositoryPaymentBill orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    // Process payment for an order

    @Transactional
    public PaymentResponse processPayment(@NotNull PaymentRequest request) {
        log.info("Processing payment for order ID: {}", request.getOrderId());

        RestaurantOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (OrderStatus.CANCELLED.name().equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELED, "Order has been canceled");
        }
        if (!OrderStatus.PENDING.name().equals(order.getStatus()) && !OrderStatus.CONFIRMED.name().equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_COMPLETED, "Order cannot be paid in current status: " + order.getStatus());
        }

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND, "Payment record not found for order: " + request.getOrderId()));

        // Tính tổng tiền dựa trên giá từ MenuItem
        BigDecimal totalAmount = orderItemRepository.findById(request.getOrderId()).stream()
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
        payment.setStatus(PaymentStatus.PAID.name());
        payment.setUpdatedAt(Instant.now()); // Cập nhật thời gian thanh toán thực tế
        paymentRepository.save(payment);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(request.getOrderId());
        orderItems.forEach(item -> {
            item.setStatus(OrderItemStatus.COMPLETED.name()); // Consider using enum if available
            orderItemRepository.save(item);
        });

        order.setStatus(OrderStatus.COMPLETED.name());
        orderRepository.save(order);

        return mapToPaymentResponse(payment);
    }

    // Cancel or refund an order
    @Transactional
    public void cancelOrRefundOrder(int orderId) {
        log.info("Canceling or refunding order ID: {}", orderId);

        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (OrderStatus.CANCELLED.name().equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELED, "Order has already been canceled");
        }
        if (OrderStatus.COMPLETED.name().equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_COMPLETED, "Cannot cancel completed order");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND, "Payment record not found for order: " + orderId));

        String paymentStatus = payment.getStatus();
        if (paymentStatus == null) {
            log.error("Payment status is null for orderId: {}", orderId);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Payment status is null");
        }
        if (PaymentStatus.PAID.name().equals(paymentStatus)) {
            payment.setStatus(PaymentStatus.REFUNDED.name());
        } else if (PaymentStatus.REFUNDED.name().equals(paymentStatus) || PaymentStatus.CANCELLED.name().equals(paymentStatus)) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PROCESSED, "Payment has already been processed");
        } else {
            payment.setStatus(PaymentStatus.CANCELLED.name());
        }
        paymentRepository.save(payment);

        orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        orderItems.forEach(item -> {
            item.setStatus(OrderItemStatus.CANCELLED.name()); // Consider using enum if available
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
                } else if (PaymentMethod.VNPAY.name().equals(payment.getPaymentMethod())) {
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
        payment.setUpdatedAt(Instant.now());
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
        response.setPaymentDate(payment.getUpdatedAt());
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
}

