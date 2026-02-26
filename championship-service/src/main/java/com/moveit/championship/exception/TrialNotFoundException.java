package com.moveit.championship.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TrialNotFoundException extends RuntimeException {
    public TrialNotFoundException(Integer id) {
        super("Manche non trouvée avec l'ID: " + id);
    }
}
