package org.mitre.clientregistry.infrastructure.adapter.in.web;

import org.mitre.clientregistry.domain.exception.ClientAlreadyExistsException;
import org.mitre.clientregistry.domain.exception.ClientNotFoundException;
import org.mitre.clientregistry.domain.exception.InvalidClientMetadataException;
import org.mitre.clientregistry.domain.exception.OptimisticLockingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleClientNotFound(ClientNotFoundException ex) {
        return ResponseEntity.status(401).body(Map.of("error", "invalid_client"));
    }

    @ExceptionHandler(InvalidClientMetadataException.class)
    public ResponseEntity<Map<String, String>> handleInvalidMetadata(InvalidClientMetadataException ex) {
        return ResponseEntity.status(400).body(Map.of(
                "error", "invalid_client_metadata",
                "error_description", ex.getMessage()
        ));
    }

    @ExceptionHandler(ClientAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleClientAlreadyExists(ClientAlreadyExistsException ex) {
        return ResponseEntity.status(409).body(Map.of("error", "conflict"));
    }

    @ExceptionHandler(OptimisticLockingException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLocking(OptimisticLockingException ex) {
        return ResponseEntity.status(409).body(Map.of("error", "conflict"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(Map.of("error", "invalid_request"));
    }
}
