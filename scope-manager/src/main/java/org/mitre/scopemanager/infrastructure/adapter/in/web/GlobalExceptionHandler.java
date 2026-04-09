package org.mitre.scopemanager.infrastructure.adapter.in.web;

import org.mitre.scopemanager.domain.exception.ScopeAlreadyExistsException;
import org.mitre.scopemanager.domain.exception.ScopeNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ScopeNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ScopeNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of(
                "error", "not_found",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(ScopeAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ScopeAlreadyExistsException ex) {
        return ResponseEntity.status(409).body(Map.of(
                "error", "conflict",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(Map.of(
                "error", "bad_request",
                "message", ex.getMessage()
        ));
    }
}
