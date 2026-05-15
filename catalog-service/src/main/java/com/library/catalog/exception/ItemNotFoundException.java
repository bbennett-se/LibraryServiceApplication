package com.library.catalog.exception;

//defines the error displayed for when an Item is not found within the database
public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String message) {
        super(message);
    }
}