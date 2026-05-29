package com.library.circulation.dto;

import com.library.circulation.entity.Loan;
import com.library.circulation.entity.LoanStatus;

import java.time.Instant;
import java.util.UUID;

public record LoanResponse(
        UUID id,
        UUID userId,
        UUID itemCopy,
        Instant checkoutDate,
        Instant dueDate,
        Instant returnDate,
        LoanStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getUserId(),
                loan.getItemCopyId(),
                loan.getCheckoutDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getLoanStatus(),
                loan.getCreatedAt(),
                loan.getUpdatedAt()
        );
    }
}
