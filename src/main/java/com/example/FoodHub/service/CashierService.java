package com.example.FoodHub.service;

import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.PaymentResponse;
import com.example.FoodHub.entity.OrderItem;
import com.example.FoodHub.entity.Payment;
import com.example.FoodHub.entity.RestaurantOrder;
import com.example.FoodHub.repository.OrderItemRepository;
import com.example.FoodHub.repository.PaymentRepository;
import com.example.FoodHub.repository.RestaurantOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
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
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"PENDING".equals(order.getStatus()) && !"CONFIRMED".equals(order.getStatus())) {
            throw new RuntimeException("Order cannot be paid in current status: " + order.getStatus());
        }

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Payment record not found for order: " + request.getOrderId()));

        BigDecimal totalAmount = orderItemRepository.calculateTotalAmountByOrderId(request.getOrderId());
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid order amount");
        }

        payment.setAmount(totalAmount);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("PAID");
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);

        order.setStatus("COMPLETED");
        orderRepository.save(order);

        return mapToPaymentResponse(payment);
    }

    // Hoàn/Hủy đơn hàng
    @Transactional
    public void cancelOrRefundOrder(Integer orderId) {
        RestaurantOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("COMPLETED".equals(order.getStatus())) {
            throw new RuntimeException("Cannot cancel completed order");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment record not found for order: " + orderId));

        if ("PAID".equals(payment.getStatus())) {
            payment.setStatus("REFUNDED");
        } else {
            payment.setStatus("CANCELLED");
        }
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);

        order.setStatus("CANCELLED");
        orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        orderItems.forEach(item -> {
            item.setStatus("CANCELLED");
            orderItemRepository.save(item);
        });
    }

    // Quản lý giao dịch (lấy danh sách giao dịch theo ngày)
    public List<PaymentResponse> getTransactionsByDate(Instant date) {
        List<Payment> payments = paymentRepository.findByCreatedAtDate(date);
        return payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    // Tính tổng doanh thu theo ngày
    public BigDecimal getTotalRevenueByDate(Instant date) {
        BigDecimal totalRevenue = paymentRepository.calculateTotalRevenueByDate(date);
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setOrderId(payment.getOrder().getId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }
}