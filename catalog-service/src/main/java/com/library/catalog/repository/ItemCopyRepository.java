package com.library.catalog.repository;

import com.library.catalog.entity.ItemCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ItemCopyRepository extends JpaRepository<ItemCopy, UUID> {

    List<ItemCopy> findByItemId(UUID itemId);

    List<ItemCopy> findByItemIdAndStatus( UUID itemId, ItemCopy.CopyStatus status);

}