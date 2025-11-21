package com.innowise.orderservice.api.dto.item;

import java.math.BigDecimal;

public record GetItemDto (
    Long id,
    String name,
    BigDecimal price
) {}
