package com.moveit.auth.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionResolver extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception exception) {
        return switch (exception) {
            case BadCredentialsException e -> createProblemDetail(401, e.getMessage(), "The username or password is incorrect");
            case AccountStatusException e -> createProblemDetail(403, e.getMessage(), "The account is locked");
            case AccessDeniedException e -> createProblemDetail(403, e.getMessage(), "You are not authorized to access this resource");
            case SignatureException e -> createProblemDetail(403, e.getMessage(), "The JWT signature is invalid");
            case ExpiredJwtException e -> createProblemDetail(403, e.getMessage(), "The JWT token has expired");
            case DataIntegrityViolationException e -> {
                String message = e.getMessage();
                if (message != null && message.contains("nickname")) {
                    yield createProblemDetail(409, "Nickname already exists", "An account with this nickname already exists");
                }
                yield createProblemDetail(409, "Data integrity violation", "The provided data conflicts with existing records");
            }
            default -> createProblemDetail(500, exception.getMessage(), "Unknown internal server error.");
        };
    }

    private ProblemDetail createProblemDetail(int status, String message, String description) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status), message);
        detail.setProperty("description", description);
        return detail;
    }
}
