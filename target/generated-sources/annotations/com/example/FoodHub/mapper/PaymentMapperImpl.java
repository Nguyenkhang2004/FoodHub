package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.PaymentResponse;
import com.example.FoodHub.entity.Payment;
import com.example.FoodHub.entity.RestaurantOrder;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-22T18:45:02+0700",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class PaymentMapperImpl implements PaymentMapper {

    @Override
    public Payment toPayment(PaymentRequest request) {
        if ( request == null ) {
            return null;
        }

        Payment payment = new Payment();

        payment.setPaymentMethod( request.getPaymentMethod() );

        return payment;
    }

    @Override
    public PaymentResponse toPaymentResponse(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        PaymentResponse paymentResponse = new PaymentResponse();

        paymentResponse.setOrderId( paymentOrderId( payment ) );
        paymentResponse.setAmount( payment.getAmount() );
        paymentResponse.setPaymentMethod( payment.getPaymentMethod() );
        paymentResponse.setTransactionId( payment.getTransactionId() );
        paymentResponse.setStatus( payment.getStatus() );
        paymentResponse.setCreatedAt( payment.getCreatedAt() );
        paymentResponse.setUpdatedAt( payment.getUpdatedAt() );
        paymentResponse.setPaymentUrl( payment.getPaymentUrl() );

        return paymentResponse;
    }

    @Override
    public void updatePaymentFromRequest(PaymentRequest request, Payment payment) {
        if ( request == null ) {
            return;
        }

        payment.setPaymentMethod( request.getPaymentMethod() );
    }

    private Integer paymentOrderId(Payment payment) {
        RestaurantOrder order = payment.getOrder();
        if ( order == null ) {
            return null;
        }
        return order.getId();
    }
}
