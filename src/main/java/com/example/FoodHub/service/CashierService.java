package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.PaymentResponse;
import com.example.FoodHub.dto.response.RevenueStatsResponseForCashier;
import com.example.FoodHub.entity.OrderItem;
import com.example.FoodHub.entity.Payment;
import com.example.FoodHub.entity.RestaurantOrder;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import com.example.FoodHub.repository.OrderItemRepository;
import com.example.FoodHub.repository.PaymentRepository;
import com.example.FoodHub.repository.RestaurantOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashierService {

    @Autowired
    private RestaurantOrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // Thanh toán đơn hàng
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        RestaurantOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_COMPLETED, "Order cannot be paid in current status: " + order.getStatus());
        }

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND, "Payment record not found for order: " + request.getOrderId()));

        BigDecimal totalAmount = orderItemRepository.calculateTotalAmountByOrderId(request.getOrderId());
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_ORDER_AMOUNT, "Invalid order amount");
        }

        payment.setAmount(totalAmount);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("PAID");
        // Không set updated_at, để MySQL tự động cập nhật
        paymentRepository.save(payment);

        // Cập nhật trạng thái của order_item
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(request.getOrderId());
        orderItems.forEach(item -> {
            item.setStatus("COMPLETED");
            // Không set updated_at, để MySQL tự động cập nhật
            orderItemRepository.save(item);
        });

        order.setStatus("COMPLETED");
        // Không set updated_at, để MySQL tự động cập nhật
        orderRepository.save(order);

        return mapToPaymentResponse(payment);
    }

    // Hoàn/Hủy đơn hàng
    @Transactional
    public void cancelOrRefundOrder(Integer orderId) {
        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if ("COMPLETED".equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_COMPLETED, "Cannot cancel completed order");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND, "Payment record not found for order: " + orderId));

        if ("PAID".equals(payment.getStatus())) {
            payment.setStatus("REFUNDED");
        } else {
            payment.setStatus("CANCELLED");
        }
        // Không set updated_at, để MySQL tự động cập nhật
        paymentRepository.save(payment);

        order.setStatus("CANCELLED");
        // Không set updated_at, để MySQL tự động cập nhật
        orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        orderItems.forEach(item -> {
            item.setStatus("CANCELLED");
            // Không set updated_at, để MySQL tự động cập nhật
            orderItemRepository.save(item);
        });
    }

    // Quản lý giao dịch (lấy danh sách giao dịch theo ngày)
    public List<PaymentResponse> getTransactionsByDate(Instant start, Instant end) {
        if (end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        List<Payment> payments = paymentRepository.findByCreatedAtBetween(start, end);
        return payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalRevenueByDate(Instant date) {
        BigDecimal totalRevenue = paymentRepository.calculateTotalRevenueByDate(date);
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public RevenueStatsResponseForCashier getRevenueStatsByDate(Instant date) {
        Instant startOfDay = date.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate()
                .atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        Instant endOfDay = date.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate()
                .plusDays(1).atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).minusSeconds(1).toInstant();

        return calculateRevenueStats(startOfDay, endOfDay);
    }

    public RevenueStatsResponseForCashier getRevenueStatsByDateRange(Instant start, Instant end) {
        if (end.isBefore(start)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }
        return calculateRevenueStats(start, end);
    }

    // Phương thức phụ để tái sử dụng logic tính toán doanh thu
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

            // Chỉ tính totalRevenue cho các giao dịch PAID
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
}