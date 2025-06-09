package com.example.FoodHub.service;

import com.example.FoodHub.entity.Payment;
import com.example.FoodHub.repository.PaymentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {

    PaymentRepository paymentRepository;

    public List<Payment> getPendingPayments() {
        log.info("Fetching pending payments");
        return paymentRepository.findByStatus("PENDING");
    }

    public Payment processPayment(Payment payment) {
        log.info("Processing payment for order ID: {}", payment.getOrder().getId());
        payment.setStatus("PAID");
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        return paymentRepository.save(payment);
    }

    public List<Payment> getAllTransactions() {
        log.info("Fetching all transactions");
        return paymentRepository.findAll();
    }

    public Payment refundPayment(Integer paymentId) {
        log.info("Refunding payment with ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus("REFUNDED");
        payment.setUpdatedAt(Instant.now());
        return paymentRepository.save(payment);
    }
}