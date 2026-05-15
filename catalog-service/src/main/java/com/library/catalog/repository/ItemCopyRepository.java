package com.library.catalog.repository;

import com.library.catalog.entity.ItemCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

//Defines the search options for finding specific Item Copies
//TODO: implement Item ID Search for copies
//TODO: Implement ID and status search for copies
@Repository
public interface ItemCopyRepository extends JpaRepository<ItemCopy, UUID> {

    List<ItemCopy> findByItemId(UUID itemId);

    List<ItemCopy> findByItemIdAndStatus( UUID itemId, ItemCopy.CopyStatus status);

}