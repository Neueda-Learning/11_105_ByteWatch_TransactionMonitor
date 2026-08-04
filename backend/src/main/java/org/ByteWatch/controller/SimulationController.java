package org.ByteWatch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ByteWatch.model.SimulationStartRequest;
import org.ByteWatch.model.SimulationStatusResponse;
import org.ByteWatch.service.TransactionSimulationService;

import java.util.Map;

/**
 * Endpoints to start/stop and inspect a live transaction simulation feed.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/simulation")
public class SimulationController {

    private final TransactionSimulationService transactionSimulationService;

    public SimulationController(TransactionSimulationService transactionSimulationService) {
        this.transactionSimulationService = transactionSimulationService;
    }

    @GetMapping("/status")
    public ResponseEntity<SimulationStatusResponse> status() {
        return ResponseEntity.ok(transactionSimulationService.currentStatus());
    }

    @PostMapping("/start")
    public ResponseEntity<SimulationStatusResponse> start(@RequestBody(required = false) SimulationStartRequest request) {
        return ResponseEntity.ok(transactionSimulationService.start(request));
    }

    @PostMapping("/stop")
    public ResponseEntity<SimulationStatusResponse> stop() {
        return ResponseEntity.ok(transactionSimulationService.stop());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
