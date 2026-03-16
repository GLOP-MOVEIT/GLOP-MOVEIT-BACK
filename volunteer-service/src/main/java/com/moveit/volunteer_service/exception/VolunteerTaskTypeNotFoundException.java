package com.moveit.volunteer_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VolunteerTaskTypeNotFoundException extends RuntimeException {
    public VolunteerTaskTypeNotFoundException(Long id) {
        super("Volunteer task type with id " + id + " not found");
    }
}
