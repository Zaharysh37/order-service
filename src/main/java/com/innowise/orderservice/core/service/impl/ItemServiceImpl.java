package com.innowise.orderservice.core.service.impl;

import com.innowise.orderservice.api.dto.item.CreateItemDto;
import com.innowise.orderservice.api.dto.item.GetItemDto;
import com.innowise.orderservice.core.dao.ItemRepository;
import com.innowise.orderservice.core.entity.Item;
import com.innowise.orderservice.core.mapper.itemmapper.CreateItemMapper;
import com.innowise.orderservice.core.mapper.itemmapper.GetItemMapper;
import com.innowise.orderservice.core.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final GetItemMapper getItemMapper;
    private final CreateItemMapper createItemMapper;

    @Override
    @Transactional
    public GetItemDto crateItem(CreateItemDto createItemDto) {
        Item item = createItemMapper.toEntity(createItemDto);

        Item savedItem = itemRepository.save(item);

        return getItemMapper.toDto(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public GetItemDto getItemById(Long id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item not found: " + id));
        return getItemMapper.toDto(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetItemDto> getAllItems() {
        List<Item> items = itemRepository.findAll();
        return getItemMapper.toDtos(items);
    }

    @Override
    @Transactional
    public GetItemDto updateItem(Long id, CreateItemDto createItemDto) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item not found: " + id));

        createItemMapper.merge(item, createItemDto);

        Item savedItem = itemRepository.save(item);

        return getItemMapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        Item existedItem = itemRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item not found: " + id));
        itemRepository.delete(existedItem);
    }
}
