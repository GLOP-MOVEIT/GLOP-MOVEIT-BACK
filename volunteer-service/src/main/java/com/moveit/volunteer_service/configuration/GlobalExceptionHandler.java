package com.moveit.volunteer_service.configuration;

import com.moveit.volunteer_service.exception.TaskAssignmentNotFoundException;
import com.moveit.volunteer_service.exception.VolunteerPreferenceNotFoundException;
import com.moveit.volunteer_service.exception.VolunteerTaskNotFoundException;
import com.moveit.volunteer_service.exception.VolunteerTaskTypeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(VolunteerTaskNotFoundException.class)
    public ResponseEntity<Void> handleVolunteerTaskNotFound(VolunteerTaskNotFoundException ex) {
        log.warn(ex.getMessage());
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(VolunteerTaskTypeNotFoundException.class)
    public ResponseEntity<Void> handleVolunteerTaskTypeNotFound(VolunteerTaskTypeNotFoundException ex) {
        log.warn(ex.getMessage());
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(VolunteerPreferenceNotFoundException.class)
    public ResponseEntity<Void> handleVolunteerPreferenceNotFound(VolunteerPreferenceNotFoundException ex) {
        log.warn(ex.getMessage());
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TaskAssignmentNotFoundException.class)
    public ResponseEntity<Void> handleTaskAssignmentNotFound(TaskAssignmentNotFoundException ex) {
        log.warn(ex.getMessage());
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn(ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
