package com.innowise.orderservice.core.service;

import com.innowise.orderservice.api.dto.order.CreateOrderDto;
import com.innowise.orderservice.api.dto.order.GetOrderDto;
import com.innowise.orderservice.core.entity.Status;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    GetOrderDto createOrder(CreateOrderDto createOrderDto);

    GetOrderDto getOrderById(Long id);

    Page<GetOrderDto> getOrdersByIds(List<Long> ids, Pageable pageable);

    Page<GetOrderDto> getOrdersByStatuses(List<Status> statuses, Pageable pageable);

    Page<GetOrderDto> getAllOrders(Pageable pageable);

    GetOrderDto updateOrderStatus(Long id, Status status);

    void deleteOrder(Long id);
}
