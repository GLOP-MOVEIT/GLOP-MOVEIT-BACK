package com.moveit.user.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException ex) {
        return createProblemDetail(HttpStatus.NOT_FOUND.value(), ex.getMessage(), "The requested user does not exist");
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ProblemDetail handleRoleNotFoundException(RoleNotFoundException ex) {
        return createProblemDetail(HttpStatus.NOT_FOUND.value(), ex.getMessage(), "The requested role does not exist");
    }

    @ExceptionHandler(RequestNotFoundException.class)
    public ProblemDetail handleRequestNotFoundException(RequestNotFoundException ex) {
        return createProblemDetail(HttpStatus.NOT_FOUND.value(), ex.getMessage(), "The requested request does not exist");
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ProblemDetail handleTicketNotFoundException(TicketNotFoundException ex) {
        return createProblemDetail(HttpStatus.NOT_FOUND.value(), ex.getMessage(), "The requested ticket does not exist");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("email")) {
            return createProblemDetail(HttpStatus.CONFLICT.value(), "Email already exists", "An account with this email already exists");
        }
        if (message != null && message.contains("nickname")) {
            return createProblemDetail(HttpStatus.CONFLICT.value(), "Nickname already exists", "An account with this nickname already exists");
        }
        return createProblemDetail(HttpStatus.CONFLICT.value(), "Data integrity violation", "The provided data conflicts with existing records");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(), "An unexpected error occurred");
    }

    private ProblemDetail createProblemDetail(int status, String message, String description) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status), message);
        detail.setProperty("description", description);
        return detail;
    }
}