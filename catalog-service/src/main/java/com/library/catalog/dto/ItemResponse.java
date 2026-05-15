package com.library.catalog.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

//specifies the required variables for an item response
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