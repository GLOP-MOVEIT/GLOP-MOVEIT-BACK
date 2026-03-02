package com.moveit.volunteer_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaskAssignmentNotFoundException extends RuntimeException {
    public TaskAssignmentNotFoundException(Long id) {
        super("Task assignment with id " + id + " not found");
    }
}
