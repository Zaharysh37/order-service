package com.innowise.orderservice.core.service.impl;

import com.innowise.orderservice.api.client.GetUserDto;
import com.innowise.orderservice.api.client.UserClient;
import com.innowise.orderservice.api.dto.order.CreateOrderDto;
import com.innowise.orderservice.api.dto.order.GetOrderDto;
import com.innowise.orderservice.api.dto.order.orderitem.CreateOrderItemDto;
import com.innowise.orderservice.core.dao.ItemRepository;
import com.innowise.orderservice.core.dao.OrderRepository;
import com.innowise.orderservice.core.entity.Item;
import com.innowise.orderservice.core.entity.Order;
import com.innowise.orderservice.core.entity.OrderItem;
import com.innowise.orderservice.core.entity.Status;
import com.innowise.orderservice.core.mapper.orderitemmapper.GetOrderItemMapper;
import com.innowise.orderservice.core.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserClient userClient;
    private final GetOrderItemMapper getOrderItemMapper;

    @Override
    @Transactional
    public GetOrderDto createOrder(CreateOrderDto createOrderDto) {
        GetUserDto getUserDto = userClient.getUserByEmail(createOrderDto.userEmail());

        if (getUserDto.id() == -1L) {
            throw new RuntimeException("User service is unavailable, cannot create order.");
        }

        Order order = new Order();
        order.setUserId(getUserDto.id());
        order.setStatus(Status.CREATED);

        for(CreateOrderItemDto itemDto : createOrderDto.items()) {

            Item item = itemRepository.findById(itemDto.itemId())
                .orElseThrow(() -> new EntityNotFoundException("Item not found: " + itemDto.itemId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(itemDto.quantity());
            orderItem.setItem(item);
            orderItem.setOrder(order);

            order.getOrderItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        return new GetOrderDto(
            savedOrder.getId(),
            savedOrder.getStatus(),
            savedOrder.getCreationDate(),
            new HashSet<>(getOrderItemMapper.toDtos(savedOrder.getOrderItems())),
            getUserDto
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GetOrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));

        GetUserDto getUserDto = userClient.getUserById(order.getUserId());

        return new GetOrderDto(
            order.getId(),
            order.getStatus(),
            order.getCreationDate(),
            new HashSet<>(getOrderItemMapper.toDtos(order.getOrderItems())),
            getUserDto
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetOrderDto> getOrdersByIds(List<Long> ids) {
        List<Order> orders = orderRepository.findAllByIdInOrderByCreationDateDesc(ids);
        return mapOrdersToGetDtos(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetOrderDto> getOrdersByStatuses(List<Status> statuses) {
        List<Order> orders = orderRepository.findAllByStatusInOrderByCreatedAtDesc(statuses);
        return mapOrdersToGetDtos(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetOrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return mapOrdersToGetDtos(orders);
    }

    private List<GetOrderDto> mapOrdersToGetDtos(List<Order> orders) {
        return orders.stream()
            .map(order -> {
                GetUserDto getUserDto = userClient.getUserById(order.getUserId());
                return new GetOrderDto(
                    order.getId(),
                    order.getStatus(),
                    order.getCreationDate(),
                    new HashSet<>(getOrderItemMapper.toDtos(order.getOrderItems())),
                    getUserDto
                );
                })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GetOrderDto updateOrderStatus(Long id, Status status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
        order.setStatus(status);

        Order savedOrder = orderRepository.save(order);

        GetUserDto getUserDto = userClient.getUserById(savedOrder.getUserId());

        return new GetOrderDto(
            savedOrder.getId(),
            savedOrder.getStatus(),
            savedOrder.getCreationDate(),
            new HashSet<>(getOrderItemMapper.toDtos(savedOrder.getOrderItems())),
            getUserDto
        );
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        Order existingOrder = orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));

        try {
            userClient.getUserById(existingOrder.getUserId());
        } catch (feign.FeignException.Forbidden e) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You are not the owner");
        }

        orderRepository.deleteById(id);
    }
}
