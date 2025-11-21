package com.innowise.orderservice.core.mapper.orderitemmapper;

import com.innowise.orderservice.api.dto.order.orderitem.GetOrderItemDto;
import com.innowise.orderservice.core.entity.OrderItem;
import com.innowise.orderservice.core.mapper.BaseMapper;
import com.innowise.orderservice.core.mapper.itemmapper.GetItemMapper;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapper.class, uses = GetItemMapper.class)
public interface GetOrderItemMapper extends BaseMapper<OrderItem, GetOrderItemDto> {
}
