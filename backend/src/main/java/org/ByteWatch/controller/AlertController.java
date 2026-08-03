package org.ByteWatch.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ByteWatch.model.AlertDetailDTO;
import org.ByteWatch.model.AlertStatusUpdateRequest;
import org.ByteWatch.service.AlertService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST endpoints for viewing active alerts, viewing a single alert's
 * full detail and audit history, and driving the alert lifecycle.
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Returns all active alerts (OPEN, ACKNOWLEDGED, INVESTIGATING),
     * enriched with transaction and customer context, for the dashboard queue.
     */
    @GetMapping
    public ResponseEntity<List<AlertDetailDTO>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    /**
     * Returns full detail for a single alert, including its complete audit history.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlertDetailDTO> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlertDetail(id));
    }

    /**
     * Updates an alert's status, enforcing the allowed lifecycle order and
     * the mandatory-comment rule for DISMISSED/CLOSED, and writes an audit log entry.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<AlertDetailDTO> updateStatus(@PathVariable Long id,
                                                         @RequestBody AlertStatusUpdateRequest request) {
        return ResponseEntity.ok(alertService.updateAlertStatus(id, request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
