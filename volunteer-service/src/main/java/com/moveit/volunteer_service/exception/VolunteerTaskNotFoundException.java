package com.moveit.volunteer_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VolunteerTaskNotFoundException extends RuntimeException {
    public VolunteerTaskNotFoundException(Long id) {
        super("Volunteer task with id " + id + " not found");
    }
}
