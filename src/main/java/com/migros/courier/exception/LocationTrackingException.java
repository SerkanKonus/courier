package com.migros.courier.exception;

/**
 * Kurye konum takibi sırasında oluşan hataları yönetmek için özel exception
 */
public class LocationTrackingException extends RuntimeException {

    public LocationTrackingException(String message, Throwable cause) {
        super(message, cause);
    }
}
