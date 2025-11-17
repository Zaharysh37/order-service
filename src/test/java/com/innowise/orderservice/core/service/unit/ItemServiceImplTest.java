package com.innowise.orderservice.core.service.unit;

import com.innowise.orderservice.api.dto.item.CreateItemDto;
import com.innowise.orderservice.api.dto.item.GetItemDto;
import com.innowise.orderservice.core.dao.ItemRepository;
import com.innowise.orderservice.core.entity.Item;
import com.innowise.orderservice.core.mapper.itemmapper.CreateItemMapper;
import com.innowise.orderservice.core.mapper.itemmapper.GetItemMapper;
import com.innowise.orderservice.core.service.impl.ItemServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private GetItemMapper getItemMapper;
    @Mock
    private CreateItemMapper createItemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createItem_shouldSaveAndReturnDto() {
        CreateItemDto createDto = new CreateItemDto("Laptop", new BigDecimal("1000.00"));
        Item itemEntity = new Item(null, "Laptop", new BigDecimal("1000.00"));
        Item savedItem = new Item(1L, "Laptop", new BigDecimal("1000.00"));
        GetItemDto expectedDto = new GetItemDto(1L, "Laptop", new BigDecimal("1000.00"));

        when(createItemMapper.toEntity(createDto)).thenReturn(itemEntity);
        when(itemRepository.save(itemEntity)).thenReturn(savedItem);
        when(getItemMapper.toDto(savedItem)).thenReturn(expectedDto);

        GetItemDto result = itemService.createItem(createDto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(itemRepository).save(itemEntity);
    }

    @Test
    void getItemById_shouldReturnDto_whenExists() {
        Long id = 1L;
        Item item = new Item(id, "Phone", BigDecimal.TEN);
        GetItemDto expectedDto = new GetItemDto(id, "Phone", BigDecimal.TEN);

        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(getItemMapper.toDto(item)).thenReturn(expectedDto);

        GetItemDto result = itemService.getItemById(id);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void getItemById_shouldThrowException_whenNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Item not found: 99");
    }

    @Test
    void updateItem_shouldUpdateAndReturnDto() {
        Long id = 1L;
        CreateItemDto updateDto = new CreateItemDto("Updated Name", BigDecimal.ONE);
        Item existingItem = new Item(id, "Old Name", BigDecimal.TEN);
        Item savedItem = new Item(id, "Updated Name", BigDecimal.ONE);
        GetItemDto expectedDto = new GetItemDto(id, "Updated Name", BigDecimal.ONE);

        when(itemRepository.findById(id)).thenReturn(Optional.of(existingItem));
        doAnswer(invocation -> {
            Item target = invocation.getArgument(0);
            CreateItemDto source = invocation.getArgument(1);
            target.setName(source.name());
            target.setPrice(source.price());
            return null;
        }).when(createItemMapper).merge(existingItem, updateDto);

        when(itemRepository.save(existingItem)).thenReturn(savedItem);
        when(getItemMapper.toDto(savedItem)).thenReturn(expectedDto);

        GetItemDto result = itemService.updateItem(id, updateDto);

        assertThat(result.name()).isEqualTo("Updated Name");
        verify(itemRepository).save(existingItem);
    }

    @Test
    void deleteItem_shouldDelete_whenExists() {
        Long id = 1L;
        Item item = new Item(id, "Val", BigDecimal.ONE);
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        itemService.deleteItem(id);

        verify(itemRepository).delete(item);
    }

    @Test
    void deleteItem_shouldThrow_whenNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.deleteItem(99L))
            .isInstanceOf(EntityNotFoundException.class);

        verify(itemRepository, never()).delete(any());
    }
}
