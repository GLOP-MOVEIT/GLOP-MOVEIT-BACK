package com.moveit.notification.exception;

/**
 * Exception levée quand les paramètres de pagination sont invalides.
 * Point 10 - Validation des limites de pagination.
 */
public class InvalidPaginationException extends RuntimeException {
    
    public InvalidPaginationException(String message) {
        super(message);
    }
    
    public InvalidPaginationException(String message, Throwable cause) {
        super(message, cause);
    }
}
