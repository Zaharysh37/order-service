package com.innowise.orderservice.api.controller;

import com.innowise.orderservice.api.dto.item.CreateItemDto;
import com.innowise.orderservice.api.dto.item.GetItemDto;
import com.innowise.orderservice.core.service.ItemService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GetItemDto> createItem(@RequestBody CreateItemDto item) {
        GetItemDto getItemDto = itemService.createItem(item);
        return new ResponseEntity<>(getItemDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetItemDto> getItemById(@PathVariable Long id) {
        GetItemDto getItemDto = itemService.getItemById(id);
        return new ResponseEntity<>(getItemDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<GetItemDto>> getAllItems() {
        List<GetItemDto> getItemDto = itemService.getAllItems();
        return new ResponseEntity<>(getItemDto, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GetItemDto> update(@PathVariable Long id,
                                             @RequestBody CreateItemDto item) {
        GetItemDto getItemDto = itemService.updateItem(id, item);
        return new ResponseEntity<>(getItemDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GetItemDto> delete(@PathVariable Long id) {
        itemService.deleteItem(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
