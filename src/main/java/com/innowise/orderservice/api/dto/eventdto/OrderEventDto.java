package com.innowise.orderservice.api.dto.eventdto;

import java.math.BigDecimal;

public record OrderEventDto(
    Long orderId,
    Long userId,
    BigDecimal amount
) {}
