package com.example.FoodHub.mapper;

import com.example.FoodHub.dto.request.PaymentRequest;
import com.example.FoodHub.dto.response.PaymentResponse;
import com.example.FoodHub.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "order", ignore = true) // Giả sử ID sẽ được set trong service
    Payment toPayment(PaymentRequest request);
    @Mapping(source = "order.id", target = "orderId")
    PaymentResponse toPaymentResponse(Payment payment);
    void updatePaymentFromRequest(PaymentRequest request, @MappingTarget Payment payment);
}
