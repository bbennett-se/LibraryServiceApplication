package com.library.catalog.repository;

import com.library.catalog.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    Page<Item> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Item> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    Page<Item> findByGenre(String genre, Pageable pageable);
}