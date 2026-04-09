package org.mitre.umaserver.infrastructure.adapter.in.web;

import org.mitre.umaserver.domain.exception.InsufficientClaimsException;
import org.mitre.umaserver.domain.exception.PermissionTicketExpiredException;
import org.mitre.umaserver.domain.exception.ResourceSetNotFoundException;
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

    @ExceptionHandler(ResourceSetNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceSetNotFound(ResourceSetNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "not_found", "error_description", ex.getMessage()));
    }

    @ExceptionHandler(PermissionTicketExpiredException.class)
    public ResponseEntity<Map<String, String>> handleTicketExpired(PermissionTicketExpiredException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "expired_ticket", "error_description", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientClaimsException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientClaims(InsufficientClaimsException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "insufficient_claims", "error_description", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "invalid_request", "error_description", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "server_error", "error_description", "An unexpected error occurred"));
    }
}
