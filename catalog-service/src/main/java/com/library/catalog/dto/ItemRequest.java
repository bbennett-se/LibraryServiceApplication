package com.library.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemRequest {

    @NotBlank(message = "Title is Required")
    @Size(max = 255)
    private String title;

    @NotBlank(message = "Author is Required")
    @Size(max = 255)
    private String author;

    @Size(max = 20)
    private String isbn;

    private Integer publicationYear;

    private String genre;

    private String description;
}