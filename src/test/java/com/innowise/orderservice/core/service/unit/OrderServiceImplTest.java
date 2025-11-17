package com.innowise.orderservice.core.service.unit;

import com.innowise.orderservice.api.client.GetUserDto;
import com.innowise.orderservice.api.client.UserClient;
import com.innowise.orderservice.api.dto.item.GetItemDto;
import com.innowise.orderservice.api.dto.order.CreateOrderDto;
import com.innowise.orderservice.api.dto.order.GetOrderDto;
import com.innowise.orderservice.api.dto.order.orderitem.CreateOrderItemDto;
import com.innowise.orderservice.api.dto.order.orderitem.GetOrderItemDto;
import com.innowise.orderservice.core.dao.ItemRepository;
import com.innowise.orderservice.core.dao.OrderRepository;
import com.innowise.orderservice.core.entity.Item;
import com.innowise.orderservice.core.entity.Order;
import com.innowise.orderservice.core.entity.Status;
import com.innowise.orderservice.core.mapper.orderitemmapper.GetOrderItemMapper;
import com.innowise.orderservice.core.service.impl.OrderServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

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
    private GetOrderItemMapper getOrderItemMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private final Long USER_ID = 100L;
    private final String USER_EMAIL = "test@mail.com";
    private final GetUserDto USER_DTO = new GetUserDto(USER_ID, "John", "Doe", null, USER_EMAIL, List.of());

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
            order.setCreationDate(Instant.now());
            return order;
        });

        when(getOrderItemMapper.toDtos(anySet())).thenReturn(List.of(new GetOrderItemDto(1L, 2, new GetItemDto(1L, "Item 1", BigDecimal.TEN))));

        GetOrderDto result = orderService.createOrder(createOrderDto);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(Status.CREATED);
        assertThat(result.user()).isEqualTo(USER_DTO);
        assertThat(result.orderItems()).hasSize(1);

        verify(userClient).getUserByEmail(USER_EMAIL);
        verify(itemRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_shouldThrowException_whenUserClientReturnsFallback() {
        CreateOrderDto createOrderDto = new CreateOrderDto("unknown@mail.com", List.of());
        GetUserDto fallbackUser = new GetUserDto(-1L, "N/A", "N/A", null, "N/A", List.of());

        when(userClient.getUserByEmail("unknown@mail.com")).thenReturn(fallbackUser);

        assertThatThrownBy(() -> orderService.createOrder(createOrderDto))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User service is unavailable, cannot create order.");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_shouldThrowException_whenItemNotFound() {
        CreateOrderItemDto itemDto = new CreateOrderItemDto(99L, 1);
        CreateOrderDto createOrderDto = new CreateOrderDto(USER_EMAIL, List.of(itemDto));

        when(userClient.getUserByEmail(USER_EMAIL)).thenReturn(USER_DTO);
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(createOrderDto))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Item not found: 99");
    }

    @Test
    void getOrderById_shouldReturnOrder_whenExists() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(USER_ID);
        order.setStatus(Status.CREATED);
        order.setCreationDate(Instant.now());
        order.setOrderItems(new HashSet<>());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userClient.getUserById(USER_ID)).thenReturn(USER_DTO);

        GetOrderDto result = orderService.getOrderById(orderId);

        assertThat(result.id()).isEqualTo(orderId);
        assertThat(result.user()).isEqualTo(USER_DTO);
    }

    @Test
    void getOrderById_shouldThrow_whenNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getOrdersByStatuses_shouldMapUsersCorrectly() {
        List<Status> statuses = List.of(Status.CREATED);
        Order order1 = new Order(); order1.setId(1L); order1.setUserId(USER_ID);
        Order order2 = new Order(); order2.setId(2L); order2.setUserId(USER_ID);

        List<Order> orders = List.of(order1, order2);

        when(orderRepository.findAllByStatusInOrderByCreatedAtDesc(statuses)).thenReturn(orders);
        when(userClient.getUserById(USER_ID)).thenReturn(USER_DTO);

        List<GetOrderDto> results = orderService.getOrdersByStatuses(statuses);

        assertThat(results).hasSize(2);
        verify(userClient, times(2)).getUserById(USER_ID);
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

        GetOrderDto result = orderService.updateOrderStatus(orderId, Status.SHIPPED);

        assertThat(result.status()).isEqualTo(Status.SHIPPED);
        verify(orderRepository).save(order);
    }

    @Test
    void deleteOrder_shouldCallUserClientAndDelete() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(USER_ID);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userClient.getUserById(USER_ID)).thenReturn(USER_DTO);
        orderService.deleteOrder(orderId);

        verify(userClient).getUserById(USER_ID);
        verify(orderRepository).deleteById(orderId);
    }
}