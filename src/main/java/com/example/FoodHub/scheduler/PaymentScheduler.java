package com.example.FoodHub.scheduler;

import com.example.FoodHub.entity.Payment;
import com.example.FoodHub.enums.PaymentStatus;
import com.example.FoodHub.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {
    private final PaymentRepository paymentRepository;

    @Scheduled(fixedRate = 5 * 60 * 1000) // mỗi 5 phút
    @Transactional
    public void cancelExpiredPayments() {
        Instant tenMinutesAgo = Instant.now().minus(Duration.ofMinutes(10));
        List<Payment> expiredPayments = paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.PENDING.name(), tenMinutesAgo
        );

        for (Payment payment : expiredPayments) {
            payment.setStatus(PaymentStatus.CANCELLED.name());
        }

        if (!expiredPayments.isEmpty()) {
            paymentRepository.saveAll(expiredPayments);
            log.info("Cancelled {} expired payment(s).", expiredPayments.size());
        }
    }
}
