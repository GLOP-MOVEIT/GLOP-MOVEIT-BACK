package com.moveit.championship.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CompetitionNotFoundException extends RuntimeException {
    public CompetitionNotFoundException(Integer id) {
        super("Competition with id " + id + " not found");
    }
}
