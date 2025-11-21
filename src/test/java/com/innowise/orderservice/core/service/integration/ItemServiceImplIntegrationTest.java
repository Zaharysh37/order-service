package com.innowise.orderservice.core.service.integration;

import com.innowise.orderservice.api.dto.item.CreateItemDto;
import com.innowise.orderservice.api.dto.item.GetItemDto;
import com.innowise.orderservice.core.dao.ItemRepository;
import com.innowise.orderservice.core.dao.OrderRepository;
import com.innowise.orderservice.core.entity.Item;
import com.innowise.orderservice.core.service.ItemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ItemServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired private ItemService itemService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ItemRepository itemRepository;

    @BeforeEach
    @AfterEach
    void cleanup() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void test_createItem_Success_Admin() {
        CreateItemDto dto = new CreateItemDto("Mouse", new BigDecimal("50.00"));
        GetItemDto created = itemService.createItem(dto);

        assertNotNull(created.id());
        assertEquals("Mouse", created.name());

        assertTrue(itemRepository.findById(created.id()).isPresent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void test_createItem_Forbidden_User() {
        CreateItemDto dto = new CreateItemDto("Mouse", new BigDecimal("50.00"));

        assertThrows(AccessDeniedException.class, () -> {
            itemService.createItem(dto);
        });
    }

    @Test
    @WithMockUser(roles = "USER")
    void test_getAllItems_Success() {
        itemRepository.save(new Item(null, "A", BigDecimal.TEN));
        itemRepository.save(new Item(null, "B", BigDecimal.TEN));

        Page<GetItemDto> page = itemService.getAllItems(PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }
}
