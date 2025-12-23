package com.innowise.orderservice.core.mapper.eventmapper;

import com.innowise.orderservice.api.dto.eventdto.OrderEventDto;
import com.innowise.orderservice.core.entity.Order;
import com.innowise.orderservice.core.entity.OrderItem;
import com.innowise.orderservice.core.mapper.BaseMapper;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapper.class)
public interface GetOrderEventMapper extends BaseMapper<Order, OrderEventDto> {

    @Override
    @Mapping(source = "id", target = "orderId")
    @Mapping(target = "amount", expression = "java(countTotalPrice(order))")
    OrderEventDto toDto(Order order);

    default BigDecimal countTotalPrice(Order order) {

        BigDecimal totalPrice = BigDecimal.ZERO;

        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return totalPrice;
        }

        for (OrderItem item : order.getOrderItems()) {
            BigDecimal itemTotal = item.getItem().getPrice()
                .multiply(new BigDecimal(item.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);
        }

        return totalPrice;
    }
}
