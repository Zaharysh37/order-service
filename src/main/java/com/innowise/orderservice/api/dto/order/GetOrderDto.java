package com.innowise.orderservice.api.dto.order;

import com.innowise.orderservice.api.client.GetUserDto;

public record GetOrderDto (
    GetOrderDtoWithoutUser getOrderDtoWithoutUser,
    GetUserDto getUserDto
) {}
