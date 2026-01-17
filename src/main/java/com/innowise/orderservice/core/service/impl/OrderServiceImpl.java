package com.innowise.orderservice.core.service.impl;

import com.innowise.orderservice.api.client.GetUserDto;
import com.innowise.orderservice.api.client.UserClient;
import com.innowise.orderservice.api.dto.eventdto.OrderEventDto;
import com.innowise.orderservice.api.dto.order.CreateOrderDto;
import com.innowise.orderservice.api.dto.order.GetOrderDto;
import com.innowise.orderservice.api.dto.order.orderitem.CreateOrderItemDto;
import com.innowise.orderservice.core.dao.ItemRepository;
import com.innowise.orderservice.core.dao.OrderRepository;
import com.innowise.orderservice.core.entity.Item;
import com.innowise.orderservice.core.entity.Order;
import com.innowise.orderservice.core.entity.OrderItem;
import com.innowise.orderservice.core.entity.Status;
import com.innowise.orderservice.core.mapper.eventmapper.GetOrderEventMapper;
import com.innowise.orderservice.core.mapper.order.GetOrderDtoWithoutUserMapper;
import com.innowise.orderservice.core.security.SecurityHelper;
import com.innowise.orderservice.core.service.OrderService;
import com.innowise.orderservice.core.service.eventservice.OrderProducer;
import jakarta.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final ItemRepository itemRepository;

    private final UserClient userClient;

    private final GetOrderDtoWithoutUserMapper getOrderDtoWithoutUserMapper;

    private final OrderProducer orderProducer;

    private final GetOrderEventMapper getOrderEventMapper;

    private final SecurityHelper securityHelper;

    @Override
    @Transactional
    public GetOrderDto createOrder(CreateOrderDto createOrderDto) {
        GetUserDto getUserDto = userClient.getUserByEmail(createOrderDto.userEmail());

        if (getUserDto == null) {
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

        OrderEventDto orderEventDto = getOrderEventMapper.toDto(order);
        orderProducer.sendOrderCreatedEvent(orderEventDto);

        return new GetOrderDto(
            getOrderDtoWithoutUserMapper.toDto(savedOrder),
            getUserDto
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GetOrderDto getOrderById(Long id) throws AccessDeniedException {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));

        checkAccess(order.getUserId());

        GetUserDto getUserDto = userClient.getUserById(order.getUserId());

        return new GetOrderDto(
            getOrderDtoWithoutUserMapper.toDto(order),
            getUserDto
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetOrderDto> getOrdersByIds(List<Long> ids, Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByIdIn(ids, pageable);
        return mapOrdersToGetDtos(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetOrderDto> getOrdersByStatuses(List<Status> statuses, Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByStatusIn(statuses, pageable);
        return mapOrdersToGetDtos(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetOrderDto> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return mapOrdersToGetDtos(orders);
    }

    private Page<GetOrderDto> mapOrdersToGetDtos(Page<Order> orders) {
        if (orders.isEmpty()) return Page.empty(orders.getPageable());

        List<GetUserDto> getUserDtos = userClient.getAllById(
            orders.getContent().stream()
                .map(Order::getUserId)
                .collect(Collectors.toList()));

        Map<Long, GetUserDto> getUserDtosMap = getUserDtos.stream()
            .collect(Collectors.toMap(
                GetUserDto::id, dto -> dto
            ));

        return orders.map(order -> new GetOrderDto(
            getOrderDtoWithoutUserMapper.toDto(order),
            getUserDtosMap.get(order.getUserId())
        ));
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
            getOrderDtoWithoutUserMapper.toDto(savedOrder),
            getUserDto
        );
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) throws AccessDeniedException {
        Order existingOrder = orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
        checkAccess(existingOrder.getUserId());
        orderRepository.delete(existingOrder);
    }

    private void checkAccess(Long userId) throws AccessDeniedException {
        Long currentUserId = securityHelper.getCurrentUserId();
        if (!securityHelper.isAdmin() && !currentUserId.equals(userId)) {
            throw new AccessDeniedException("You are not allowed to access this resource.");
        }
    }
}
