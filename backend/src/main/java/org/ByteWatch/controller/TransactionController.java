package org.ByteWatch.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ByteWatch.model.Transaction;
import org.ByteWatch.model.TransactionLiveViewDTO;
import org.ByteWatch.service.AlertService;
import org.ByteWatch.service.TransactionFeedService;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for submitting incoming transactions to be persisted
 * and evaluated by the rule engine.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/transactions")
public class TransactionController {

    private final AlertService alertService;
    private final TransactionFeedService transactionFeedService;

    public TransactionController(AlertService alertService,
                                 TransactionFeedService transactionFeedService) {
        this.alertService = alertService;
        this.transactionFeedService = transactionFeedService;
    }

    /**
     * Returns most recent transactions for realtime polling views.
     */
    @GetMapping("/live")
    public ResponseEntity<List<TransactionLiveViewDTO>> getLiveTransactions(
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(transactionFeedService.getRecentTransactions(limit));
    }

    /**
     * Accepts a transaction, persists it, runs the rule engine, and
     * automatically opens an alert if any rule was triggered.
     */
    @PostMapping
    public ResponseEntity<AlertService.TransactionIntakeResult> submitTransaction(@RequestBody Transaction transaction) {
        // Single entry point for Stage 2 ingestion + Stage 3 rule evaluation.
        AlertService.TransactionIntakeResult result = alertService.processTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        // Keep validation errors consistent for API clients.
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
