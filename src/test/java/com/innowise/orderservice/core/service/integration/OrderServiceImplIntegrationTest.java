package com.innowise.orderservice.core.service.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.orderservice.api.dto.item.CreateItemDto;
import com.innowise.orderservice.api.dto.item.GetItemDto;
import com.innowise.orderservice.api.dto.order.CreateOrderDto;
import com.innowise.orderservice.api.dto.order.GetOrderDto;
import com.innowise.orderservice.api.dto.order.orderitem.CreateOrderItemDto;
import com.innowise.orderservice.core.dao.ItemRepository;
import com.innowise.orderservice.core.dao.OrderRepository;
import com.innowise.orderservice.core.entity.Item;
import com.innowise.orderservice.core.entity.Order;
import com.innowise.orderservice.core.entity.Status;
import com.innowise.orderservice.core.service.ItemService;
import com.innowise.orderservice.core.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired private OrderService orderService;
    @Autowired private ItemService itemService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ItemRepository itemRepository;

    @BeforeEach
    @AfterEach
    void cleanup() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
    }

    private CreateItemDto createTestItemDto() {
        return new CreateItemDto("MacBook", new BigDecimal("2000.00"));
    }

    private void stubUserServiceByEmail(String email, Long userId) {
        stubFor(WireMock.get(urlPathEqualTo("/api/users/email"))
            .withQueryParam("email", equalTo(email))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                    {
                        "id": %d,
                        "name": "Test",
                        "surname": "User",
                        "email": "%s",
                        "cards": []
                    }
                    """.formatted(userId, email))));
    }

    private void stubUserServiceById(Long userId) {
        stubFor(WireMock.get(urlPathEqualTo("/api/users/" + userId))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                    {
                        "id": %d,
                        "name": "Test",
                        "surname": "User",
                        "email": "test@mail.com",
                        "cards": []
                    }
                    """.formatted(userId))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void test_createItem_Success() {
        CreateItemDto dto = createTestItemDto();
        GetItemDto created = itemService.createItem(dto);

        assertNotNull(created.id());
        assertEquals("MacBook", created.name());
        assertTrue(itemRepository.findById(created.id()).isPresent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void test_createItem_Forbidden_ForUser() {
        CreateItemDto dto = createTestItemDto();

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            itemService.createItem(dto);
        });
    }

    @Test
    @WithMockUser(roles = "USER")
    void test_createOrder_Success() {
        Item item = new Item();
        item.setName("Phone");
        item.setPrice(BigDecimal.TEN);
        item = itemRepository.save(item);

        String userEmail = "ivan@mail.com";
        Long userId = 101L;
        CreateOrderDto orderDto = new CreateOrderDto(
            userEmail,
            List.of(new CreateOrderItemDto(item.getId(), 2))
        );

        stubUserServiceByEmail(userEmail, userId);

        GetOrderDto result = orderService.createOrder(orderDto);

        assertNotNull(result.id());
        assertEquals(Status.CREATED, result.status());
        assertEquals(userId, result.user().id());
        assertEquals(1, result.orderItems().size());
    }

    @Test
    @WithMockUser(roles = "USER")
    void test_getOrderById_Success() {
        Order order = new Order();
        order.setUserId(55L);
        order.setStatus(Status.SHIPPED);
        order = orderRepository.save(order);

        stubUserServiceById(55L);

        GetOrderDto result = orderService.getOrderById(order.getId());

        assertEquals(order.getId(), result.id());
        assertEquals(55L, result.user().id());
        assertEquals("Test", result.user().name());
    }

    @Test
    @WithMockUser(roles = "USER")
    void test_deleteOrder_Success() {
        Order order = new Order();
        order.setUserId(55L);
        order.setStatus(Status.CREATED);
        order = orderRepository.save(order);

        stubUserServiceById(55L);

        orderService.deleteOrder(order.getId());

        assertTrue(orderRepository.findById(order.getId()).isEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void test_deleteOrder_Forbidden_IfUserServiceRejects() {
        Order order = new Order();
        order.setUserId(999L);
        order.setStatus(Status.CREATED);
        orderRepository.save(order);

        stubFor(WireMock.get(urlPathEqualTo("/api/users/999"))
            .willReturn(aResponse().withStatus(403)));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            orderService.deleteOrder(order.getId());
        });

        assertTrue(orderRepository.findById(order.getId()).isPresent());
    }
}