package com.innowise.orderservice.core.service.unit;

import com.innowise.orderservice.api.client.GetUserDto;
import com.innowise.orderservice.api.client.UserClient;
import com.innowise.orderservice.api.dto.eventdto.OrderEventDto;
import com.innowise.orderservice.api.dto.order.CreateOrderDto;
import com.innowise.orderservice.api.dto.order.GetOrderDto;
import com.innowise.orderservice.api.dto.order.GetOrderDtoWithoutUser;
import com.innowise.orderservice.api.dto.order.orderitem.CreateOrderItemDto;
import com.innowise.orderservice.core.dao.ItemRepository;
import com.innowise.orderservice.core.dao.OrderRepository;
import com.innowise.orderservice.core.entity.Item;
import com.innowise.orderservice.core.entity.Order;
import com.innowise.orderservice.core.entity.Status;
import com.innowise.orderservice.core.mapper.eventmapper.GetOrderEventMapper;
import com.innowise.orderservice.core.mapper.order.GetOrderDtoWithoutUserMapper;
import com.innowise.orderservice.core.security.SecurityHelper;
import com.innowise.orderservice.core.service.eventservice.OrderProducer;
import com.innowise.orderservice.core.service.impl.OrderServiceImpl;
import java.nio.file.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserClient userClient;
    @Mock
    private GetOrderDtoWithoutUserMapper getOrderDtoWithoutUserMapper;
    @Mock
    private GetOrderEventMapper getOrderEventMapper;
    @Mock
    private OrderProducer orderProducer;
    @Mock
    private SecurityHelper securityHelper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private final Long USER_ID = 100L;
    private final Long FAKE_USER_ID = 200L;
    private final String USER_EMAIL = "test@mail.com";
    private final GetUserDto USER_DTO = new GetUserDto(USER_ID, "John", "Doe", null, USER_EMAIL, List.of());

    private final GetOrderDtoWithoutUser ORDER_WITHOUT_USER_DTO = new GetOrderDtoWithoutUser(
        1L, Status.CREATED, Instant.now(), new HashSet<>()
    );

    @Test
    void createOrder_shouldCreateOrder_whenUserAndItemsExist() {
        CreateOrderItemDto itemDto = new CreateOrderItemDto(1L, 2);
        CreateOrderDto createOrderDto = new CreateOrderDto(USER_EMAIL, List.of(itemDto));
        Item itemEntity = new Item(1L, "Item 1", BigDecimal.TEN);

        when(userClient.getUserByEmail(USER_EMAIL)).thenReturn(USER_DTO);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(itemEntity));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        when(getOrderEventMapper.toDto(any(Order.class))).thenAnswer(
            invocation -> {
                Order orderLocal = invocation.getArgument(0);
                return new OrderEventDto(
                    orderLocal.getId(),
                    orderLocal.getUserId(),
                    BigDecimal.ZERO
                );
            }
        );
        doNothing().when(orderProducer).sendOrderCreatedEvent(any(OrderEventDto.class));

        when(getOrderDtoWithoutUserMapper.toDto(any(Order.class))).thenReturn(ORDER_WITHOUT_USER_DTO);

        GetOrderDto result = orderService.createOrder(createOrderDto);

        assertThat(result.getOrderDtoWithoutUser()).isEqualTo(ORDER_WITHOUT_USER_DTO);
        assertThat(result.getUserDto()).isEqualTo(USER_DTO);

        verify(userClient).getUserByEmail(USER_EMAIL);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_shouldThrowException_whenUserClientReturnsFallback() {
        CreateOrderDto createOrderDto = new CreateOrderDto("unknown@mail.com", List.of());

        when(userClient.getUserByEmail("unknown@mail.com")).thenReturn(null);

        assertThatThrownBy(() -> orderService.createOrder(createOrderDto))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User service is unavailable, cannot create order.");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderById_shouldReturnOrder_whenExists() throws AccessDeniedException {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(USER_ID);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        when(securityHelper.isAdmin()).thenReturn(false);
        when(securityHelper.getCurrentUserId()).thenReturn(USER_ID);

        when(userClient.getUserById(USER_ID)).thenReturn(USER_DTO);
        when(getOrderDtoWithoutUserMapper.toDto(order)).thenReturn(ORDER_WITHOUT_USER_DTO);

        GetOrderDto result = orderService.getOrderById(orderId);

        assertThat(result.getOrderDtoWithoutUser()).isEqualTo(ORDER_WITHOUT_USER_DTO);
        assertThat(result.getUserDto()).isEqualTo(USER_DTO);
    }

    @Test
    void getOrderById_shouldThrowAccessDenied_whenResourceDontBelongTo()
        throws AccessDeniedException {

        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(USER_ID);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        when(securityHelper.isAdmin()).thenReturn(false);
        when(securityHelper.getCurrentUserId()).thenReturn(FAKE_USER_ID);

        assertThatThrownBy(() -> orderService.getOrderById(orderId))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("You are not allowed to access this resource.");
    }

    @Test
    void getAllOrders_shouldReturnPageAndBatchFetchUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Order order1 = new Order(); order1.setId(1L); order1.setUserId(101L);
        Order order2 = new Order(); order2.setId(2L); order2.setUserId(102L);
        Page<Order> page = new PageImpl<>(List.of(order1, order2), pageable, 2);

        GetUserDto user1 = new GetUserDto(101L, "U1", "S1", null, "e1", List.of());
        GetUserDto user2 = new GetUserDto(102L, "U2", "S2", null, "e2", List.of());

        when(orderRepository.findAll(pageable)).thenReturn(page);

        when(userClient.getAllById(anyList())).thenReturn(List.of(user1, user2));

        when(getOrderDtoWithoutUserMapper.toDto(any(Order.class))).thenReturn(ORDER_WITHOUT_USER_DTO);

        Page<GetOrderDto> result = orderService.getAllOrders(pageable);

        assertThat(result).hasSize(2);
        assertThat(result.getContent().get(0).getUserDto().id()).isEqualTo(101L);

        verify(userClient).getAllById(anyList());
    }

    @Test
    void getOrdersByIds_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Long> ids = List.of(1L);
        Order order = new Order(); order.setId(1L); order.setUserId(USER_ID);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findAllByIdIn(ids, pageable)).thenReturn(page);

        when(userClient.getAllById(anyList())).thenReturn(List.of(USER_DTO));

        when(getOrderDtoWithoutUserMapper.toDto(order)).thenReturn(ORDER_WITHOUT_USER_DTO);

        Page<GetOrderDto> result = orderService.getOrdersByIds(ids, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getUserDto()).isEqualTo(USER_DTO);
    }

    @Test
    void updateOrderStatus_shouldUpdateAndReturn() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(USER_ID);
        order.setStatus(Status.CREATED);

        Order savedOrder = new Order();
        savedOrder.setId(orderId);
        savedOrder.setUserId(USER_ID);
        savedOrder.setStatus(Status.SHIPPED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(savedOrder);
        when(userClient.getUserById(USER_ID)).thenReturn(USER_DTO);

        when(getOrderDtoWithoutUserMapper.toDto(savedOrder)).thenReturn(ORDER_WITHOUT_USER_DTO);

        GetOrderDto result = orderService.updateOrderStatus(orderId, Status.SHIPPED);

        assertThat(result.getOrderDtoWithoutUser()).isEqualTo(ORDER_WITHOUT_USER_DTO);
        assertThat(result.getUserDto()).isEqualTo(USER_DTO);

        verify(orderRepository).save(order);
    }

    @Test
    void deleteOrder_shouldDeleteOrderById_whenResourceBelongTo() throws AccessDeniedException {

        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(USER_ID);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        when(securityHelper.isAdmin()).thenReturn(false);
        when(securityHelper.getCurrentUserId()).thenReturn(USER_ID);

        orderService.deleteOrder(orderId);

        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    void deleteOrder_shouldThrowAccessDenied_whenResourceDontBelongTo()
        throws AccessDeniedException {

        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(USER_ID);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        when(securityHelper.isAdmin()).thenReturn(false);
        when(securityHelper.getCurrentUserId()).thenReturn(FAKE_USER_ID);

        assertThatThrownBy(() -> orderService.deleteOrder(orderId))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("You are not allowed to access this resource.");
    }
}