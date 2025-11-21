package com.innowise.orderservice.core.mapper.order;

import com.innowise.orderservice.api.dto.order.GetOrderDtoWithoutUser;
import com.innowise.orderservice.core.entity.Order;
import com.innowise.orderservice.core.mapper.BaseMapper;
import com.innowise.orderservice.core.mapper.orderitemmapper.GetOrderItemMapper;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapper.class, uses = GetOrderItemMapper.class)
public interface GetOrderDtoWithoutUserMapper extends BaseMapper<Order, GetOrderDtoWithoutUser> {
}
