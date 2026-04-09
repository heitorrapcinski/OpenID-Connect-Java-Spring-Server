package org.mitre.authserver.infrastructure.adapter.in.web;

import org.mitre.authserver.domain.exception.AuthorizationCodeReusedException;
import org.mitre.authserver.domain.exception.ClientNotFoundException;
import org.mitre.authserver.domain.exception.InvalidGrantException;
import org.mitre.authserver.domain.exception.InvalidScopeException;
import org.mitre.authserver.domain.exception.OptimisticLockingException;
import org.mitre.authserver.domain.exception.TokenExpiredException;
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

    @ExceptionHandler(InvalidGrantException.class)
    public ResponseEntity<Map<String, String>> handleInvalidGrant(InvalidGrantException ex) {
        return error(HttpStatus.BAD_REQUEST, "invalid_grant", ex.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Map<String, String>> handleTokenExpired(TokenExpiredException ex) {
        return error(HttpStatus.UNAUTHORIZED, "invalid_token", ex.getMessage());
    }

    @ExceptionHandler(AuthorizationCodeReusedException.class)
    public ResponseEntity<Map<String, String>> handleCodeReused(AuthorizationCodeReusedException ex) {
        return error(HttpStatus.BAD_REQUEST, "invalid_grant", ex.getMessage());
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleClientNotFound(ClientNotFoundException ex) {
        return error(HttpStatus.UNAUTHORIZED, "invalid_client", ex.getMessage());
    }

    @ExceptionHandler(InvalidScopeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidScope(InvalidScopeException ex) {
        return error(HttpStatus.BAD_REQUEST, "invalid_scope", ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLocking(OptimisticLockingException ex) {
        return error(HttpStatus.CONFLICT, "conflict", ex.getMessage());
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
