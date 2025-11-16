package com.innowise.orderservice.api.dto.order;

import com.innowise.orderservice.api.client.GetUserDto;
import com.innowise.orderservice.api.dto.order.orderitem.GetOrderItemDto;
import com.innowise.orderservice.core.entity.Status;
import java.time.Instant;
import java.util.Set;

public record GetOrderDto (
    Long id,
    Status status,
    Instant creationDate,
    Set<GetOrderItemDto> orderItems,
    GetUserDto user
) {}
