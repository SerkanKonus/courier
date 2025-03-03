package com.migros.courier.exception;

/**
 * Mağaza başlatma hatalarını yönetmek için özel exception
 */
public class StoreInitializationException extends RuntimeException {
    public StoreInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
} 