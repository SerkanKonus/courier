package com.migros.courier.exception;

/**
 * Mağaza giriş kayıt hatalarını yönetmek için özel exception
 */
public class StoreEntryException extends RuntimeException {
    public StoreEntryException(String message, Throwable cause) {
        super(message, cause);
    }
} 