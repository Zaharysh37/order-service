package com.innowise.orderservice.core.service;

import com.innowise.orderservice.api.dto.order.CreateOrderDto;
import com.innowise.orderservice.api.dto.order.GetOrderDto;
import com.innowise.orderservice.core.entity.Status;
import java.util.List;

public interface OrderService {

    GetOrderDto createOrder(CreateOrderDto createOrderDto);

    GetOrderDto getOrderById(Long id);

    List<GetOrderDto> getOrdersByIds(List<Long> ids);

    List<GetOrderDto> getOrdersByStatuses(List<Status> statuses);

    List<GetOrderDto> getAllOrders();

    GetOrderDto updateOrderStatus(Long id, Status status);

    void deleteOrder(Long id);
}
