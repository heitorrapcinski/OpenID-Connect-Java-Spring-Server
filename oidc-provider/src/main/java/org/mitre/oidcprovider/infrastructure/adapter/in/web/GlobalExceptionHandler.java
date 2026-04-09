package org.mitre.oidcprovider.infrastructure.adapter.in.web;

import org.mitre.oidcprovider.domain.exception.UserInfoNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserInfoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserInfoNotFound(UserInfoNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "not_found", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, "invalid_request", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "server_error", "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String error, String description) {
        return ResponseEntity.status(status).body(Map.of(
                "error", error,
                "error_description", description != null ? description : ""
        ));
    }
}
