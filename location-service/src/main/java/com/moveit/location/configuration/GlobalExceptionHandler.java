package com.moveit.location.configuration;

import com.moveit.location.exception.LocationNotFoundException;
import com.moveit.location.service.exception.UserServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<Void> handleLocationNotFound(LocationNotFoundException ex) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<String> handleUserServiceException(UserServiceException ex) {
        String msg = ex.getMessage();
        if (msg != null && (msg.contains("Impossible de vérifier")
                || msg.contains("Non autorisé")
                || msg.contains("accepté"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
    }
}
