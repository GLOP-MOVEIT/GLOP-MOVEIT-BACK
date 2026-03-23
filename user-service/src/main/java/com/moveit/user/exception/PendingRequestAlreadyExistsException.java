package com.moveit.user.exception;

public class PendingRequestAlreadyExistsException extends RuntimeException {

    public PendingRequestAlreadyExistsException(String message) {
        super(message);
    }
}
