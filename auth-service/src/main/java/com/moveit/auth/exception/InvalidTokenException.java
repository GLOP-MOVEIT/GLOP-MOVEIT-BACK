package com.moveit.auth.exception;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}