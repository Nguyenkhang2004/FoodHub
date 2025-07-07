package com.example.FoodHub.scheduler;

import com.example.FoodHub.entity.Payment;
import com.example.FoodHub.enums.PaymentStatus;
import com.example.FoodHub.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {
    private final PaymentRepository paymentRepository;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void cancelExpiredPayments() {
        Instant tenMinutesAgo = Instant.now().minus(Duration.ofMinutes(10));
        PageRequest pageRequest = PageRequest.of(0, 1000);

        Page<Payment> expiredPayments = paymentRepository
                .findByStatusInAndCreatedAtBefore(
                        PaymentStatus.cancellableStatuses(), tenMinutesAgo, pageRequest
                );

        List<Payment> paymentsToCancel = expiredPayments.getContent();

        for (Payment payment : paymentsToCancel) {
            payment.setStatus(PaymentStatus.CANCELLED.name());
        }

        if (!paymentsToCancel.isEmpty()) {
            paymentRepository.saveAll(paymentsToCancel);
            log.info("Huỷ {} payment quá hạn.", paymentsToCancel.size());
        }
    }

}
