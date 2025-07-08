package com.example.FoodHub.utils;

import com.example.FoodHub.entity.Payment;
import com.example.FoodHub.exception.AppException;
import com.example.FoodHub.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.PaymentData;
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOSUtils {
    PayOS payOS;
    public String generatePaymentUrl(Payment payment) {
        try {
            log.info("Creating PayOS link with: orderId={}, amount={}, returnUrl={}, cancelUrl={}",
                    payment.getOrder().getId(),
                    payment.getAmount(),
                    "http://127.0.0.1:5500/payment/result.html?orderId=" + payment.getOrder().getId(),
                    "http://127.0.0.1:5500/payment/result.html?orderId=" + payment.getOrder().getId());
            PaymentData data = PaymentData.builder()
                    .orderCode(payment.getOrder().getId().longValue())
                    .amount(payment.getAmount().intValue())
                    .description("Thanh toán đơn hàng #" + payment.getOrder().getId())
                    .returnUrl("http://127.0.0.1:5500/payment/result.html?orderId=" + payment.getOrder().getId())
                    .cancelUrl("http://127.0.0.1:5500/payment/result.html?orderId=" + payment.getOrder().getId())
                    .build();

            CheckoutResponseData responseData = payOS.createPaymentLink(data);
            return responseData.getCheckoutUrl();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getTransactionId(String paymentUrl) {
        if (paymentUrl == null || paymentUrl.trim().isEmpty()) return null;

        String[] parts = paymentUrl.split("/");
        return (parts.length > 0) ? parts[parts.length - 1] : null;
    }


}
