package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.PaymentResponse;
import com.example.FoodHub.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment toPayment(PaymentRequest request);
    PaymentResponse toPaymentResponse(Payment payment);
    void updatePaymentFromRequest(PaymentRequest request, @MappingTarget Payment payment);
}
