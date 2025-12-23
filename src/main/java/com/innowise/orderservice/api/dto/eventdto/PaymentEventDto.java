package com.innowise.orderservice.api.dto.eventdto;

public record PaymentEventDto(
    String paymentId,
    Long orderId,
    Long userId,
    PaymentStatus status
) {}
