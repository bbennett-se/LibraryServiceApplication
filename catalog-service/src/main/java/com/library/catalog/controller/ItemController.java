package com.library.catalog.controller;

import com.library.catalog.dto.ItemRequest;
import com.library.catalog.dto.ItemResponse;

import com.library.catalog.repository.ItemCopyRepository;
import com.library.catalog.service.ItemService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/catalog/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse createItem(@Valid @RequestBody ItemRequest request) {
        return itemService.createItem(request);
    }

    @GetMapping("/{id}")
    public ItemResponse getItem(@PathVariable UUID id) {
        return itemService.getItem(id);
    }

    @GetMapping
    public Page<ItemResponse> getAllItems(
            @RequestParam(required = false) String title,
            Pageable pageable) {
        if(title != null && !title.isBlank()) {
            return itemService.searchByTitle(title, pageable);
        }
        return itemService.getAllItems(pageable);
    }

    @PutMapping("/{id}")
    public ItemResponse updateItem(
            @PathVariable UUID id,
            @Valid @RequestBody ItemRequest request) {
        return itemService.updateItem(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable UUID id) {
        itemService.deleteItem(id);
    }
}
