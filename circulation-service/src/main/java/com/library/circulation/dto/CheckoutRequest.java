package com.library.circulation.dto;

import com.library.circulation.entity.LoanStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CheckoutRequest(
        @NotNull UUID userId,
        @NotNull UUID itemCopyId
        ) {}
