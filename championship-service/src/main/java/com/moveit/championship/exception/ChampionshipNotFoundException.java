package com.moveit.championship.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ChampionshipNotFoundException extends RuntimeException {
    public ChampionshipNotFoundException(Integer id) {
        super("Championship with id " + id + " not found");
    }
}