package com.library.catalog.service;

import com.library.catalog.dto.ItemRequest;
import com.library.catalog.dto.ItemResponse;
import com.library.catalog.entity.Item;
import com.library.catalog.exception.ItemNotFoundException;
import com.library.catalog.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemResponse createItem(ItemRequest request) {
        Item item = Item.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publicationYear(request.getPublicationYear())
                .genre(request.getGenre())
                .description(request.getDescription())
                .build();

        Item saved = itemRepository.save(item);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ItemResponse getItem(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found: " + id));
        return toResponse(item);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable).map(this::toResponse);

    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> searchByTitle(String title, Pageable pageable)  {
        return itemRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(this::toResponse);
    }

    public ItemResponse updateItem(UUID id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found: " + id));

        item.setTitle(request.getTitle());
        item.setAuthor(request.getAuthor());
        item.setIsbn(request.getIsbn());
        item.setPublicationYear(request.getPublicationYear());
        item.setGenre(request.getGenre());
        item.setDescription(request.getDescription());

        Item updated = itemRepository.save(item);
        return toResponse(updated);
    }

    public void deleteItem(UUID id) {
        if (!itemRepository.existsById(id)) {
            throw new ItemNotFoundException("item not found: " + id);
        }
        itemRepository.deleteById(id);
    }

    private ItemResponse toResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .author(item.getAuthor())
                .isbn(item.getIsbn())
                .publicationYear(item.getPublicationYear())
                .genre(item.getGenre())
                .description(item.getDescription())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .copyCount(item.getCopies() != null ? item.getCopies().size() : 0)
                .build();

    }
}