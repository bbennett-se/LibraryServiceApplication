package com.library.catalog.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ItemResponse {
    private UUID id;
    private String title;
    private String author;
    private String isbn;
    private Integer publicationYear;
    private String genre;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int copyCount;

}