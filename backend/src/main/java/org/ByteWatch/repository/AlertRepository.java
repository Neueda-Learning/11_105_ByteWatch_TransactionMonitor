package org.ByteWatch.repository;

import org.ByteWatch.model.Alert;
import org.ByteWatch.model.AlertDetailDTO;

import java.util.List;
import java.util.Optional;

/**
 * Data access contract for the {@code alerts} table, including the
 * enriched, joined queries (alert + transaction + payer/payee
 * customer context) that back the alert list and detail endpoints.
 */
public interface AlertRepository {

    /**
     * Inserts a new alert and returns its generated ID.
     */
    Long insert(Alert alert);

    /**
     * Fetches the raw alert entity by ID (used internally for status
     * transition validation before updating).
     */
    Optional<Alert> findById(Long id);

    /**
     * Updates only the status column of an existing alert.
     */
    void updateStatus(Long id, String newStatus);

    /**
     * Returns all alerts currently in an active state (OPEN,
     * ACKNOWLEDGED, INVESTIGATING), enriched with transaction and
     * payer/payee customer details, newest first.
     */
    List<AlertDetailDTO> findActiveAlertDetails();

    /**
     * Returns the enriched detail view for a single alert (audit logs
     * are attached separately by the service layer).
     */
    Optional<AlertDetailDTO> findAlertDetailById(Long id);
}
