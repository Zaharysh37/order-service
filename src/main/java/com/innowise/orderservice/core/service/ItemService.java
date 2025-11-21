package com.innowise.orderservice.core.service;

import com.innowise.orderservice.api.dto.item.CreateItemDto;
import com.innowise.orderservice.api.dto.item.GetItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ItemService {

    @PreAuthorize("hasRole('ADMIN')")
    GetItemDto createItem(CreateItemDto createItemDto);

    GetItemDto getItemById(Long id);

    Page<GetItemDto> getAllItems(Pageable pageable);

    @PreAuthorize("hasRole('ADMIN')")
    GetItemDto updateItem(Long id, CreateItemDto createItemDto);

    @PreAuthorize("hasRole('ADMIN')")
    void deleteItem(Long id);
}
