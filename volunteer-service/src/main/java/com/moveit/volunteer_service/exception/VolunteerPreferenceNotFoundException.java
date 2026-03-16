package com.moveit.volunteer_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VolunteerPreferenceNotFoundException extends RuntimeException {
    public VolunteerPreferenceNotFoundException(Long id) {
        super("Volunteer preference with id " + id + " not found");
    }
}
