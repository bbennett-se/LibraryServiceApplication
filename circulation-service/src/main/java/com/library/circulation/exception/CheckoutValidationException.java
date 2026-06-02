package com.library.circulation.exception;

public class CheckoutValidationException extends RuntimeException {
    public CheckoutValidationException(String message) {
        super(message);
    }
}
