package com.library.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//Defines the Item Copy Class
@Entity
@Table(name = "item_copies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

//TODO: Remove Getter/Setter Annotation; generate with alt+insert
public class ItemCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false, unique = true)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CopyStatus status;

    private String location;

    @Enumerated(EnumType.STRING)
    private CopyCondition condition;

    @Column(name = "acquired_date")
    private LocalDate acquiredDate;

    public enum CopyStatus {
        AVAILABLE, CHECKED_OUT, ON_HOLD, LOST, MAINTENANCE
    }

    public enum CopyCondition {
        NEW, GOOD, FAIR, POOR
    }
}