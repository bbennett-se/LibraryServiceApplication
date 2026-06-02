package com.library.circulation.exception;

import java.util.UUID;

public class LoanNotFoundException extends RuntimeException {
    public LoanNotFoundException(UUID id) {
        super("Loan not found with id: " + id);
    }
}
