package org.mitre.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Fallback controller invoked by the circuit breaker when a downstream service is unavailable.
 * Returns HTTP 503 Service Unavailable with a JSON error body.
 */
@RestController
public class FallbackController {

    @RequestMapping("/fallback/503")
    public ResponseEntity<Map<String, String>> serviceUnavailable() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "service_unavailable",
                        "error_description", "The requested service is temporarily unavailable. Please try again later."
                ));
    }
}
