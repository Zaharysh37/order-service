package com.innowise.orderservice.api.dto.order.orderitem;

import com.innowise.orderservice.api.dto.item.GetItemDto;

public record GetOrderItemDto (
    Long id,
    Integer quantity,
    GetItemDto item
) {}
