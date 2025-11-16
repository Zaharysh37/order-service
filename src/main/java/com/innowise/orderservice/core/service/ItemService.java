package com.innowise.orderservice.core.service;

import com.innowise.orderservice.api.dto.item.CreateItemDto;
import com.innowise.orderservice.api.dto.item.GetItemDto;
import java.util.List;

public interface ItemService {

    GetItemDto crateItem(CreateItemDto createItemDto);

    GetItemDto getItemById(Long id);

    List<GetItemDto> getAllItems();

    GetItemDto updateItem(Long id, CreateItemDto createItemDto);

    void deleteItem(Long id);
}
