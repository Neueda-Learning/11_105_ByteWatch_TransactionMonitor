package org.ByteWatch.service;

import org.springframework.stereotype.Service;
import org.ByteWatch.model.Alert;
import org.ByteWatch.model.AlertDetailDTO;
import org.ByteWatch.model.AlertLog;
import org.ByteWatch.model.AlertStatusUpdateRequest;
import org.ByteWatch.model.Transaction;
import org.ByteWatch.repository.AlertLogRepository;
import org.ByteWatch.repository.AlertRepository;
import org.ByteWatch.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Handles transaction intake (persisting + running the rule engine +
 * generating alerts), alert lifecycle status transitions, and the
 * mandatory audit trail written on every status change.
 */
@Service
public class AlertService {

    /** Default status assigned to a persisted transaction when none is supplied. */
    private static final String DEFAULT_TRANSACTION_STATUS = "COMPLETED";

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_ACKNOWLEDGED = "ACKNOWLEDGED";
    private static final String STATUS_INVESTIGATING = "INVESTIGATING";
    private static final String STATUS_DISMISSED = "DISMISSED";
    private static final String STATUS_CLOSED = "CLOSED";

    /** Allowed forward transitions: OPEN -> ACKNOWLEDGED -> INVESTIGATING -> (DISMISSED | CLOSED). */
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            STATUS_OPEN, Set.of(STATUS_ACKNOWLEDGED),
            STATUS_ACKNOWLEDGED, Set.of(STATUS_INVESTIGATING),
            STATUS_INVESTIGATING, Set.of(STATUS_DISMISSED, STATUS_CLOSED)
    );

    /** Terminal statuses that require a mandatory comment/reason for the audit trail. */
    private static final Set<String> COMMENT_REQUIRED_STATUSES = Set.of(STATUS_DISMISSED, STATUS_CLOSED);

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final AlertLogRepository alertLogRepository;
    private final RuleEngineService ruleEngineService;

    public AlertService(TransactionRepository transactionRepository,
                         AlertRepository alertRepository,
                         AlertLogRepository alertLogRepository,
                         RuleEngineService ruleEngineService) {
        this.transactionRepository = transactionRepository;
        this.alertRepository = alertRepository;
        this.alertLogRepository = alertLogRepository;
        this.ruleEngineService = ruleEngineService;
    }

    /**
     * Persists an incoming transaction, runs it through the rule engine,
     * and automatically opens an alert if any rule was triggered.
     */
    public TransactionIntakeResult processTransaction(Transaction txn) {
        if (txn.getStatus() == null || txn.getStatus().isBlank()) {
            txn.setStatus(DEFAULT_TRANSACTION_STATUS);
        }
        transactionRepository.insert(txn);

        RuleEngineService.RuleEvaluationResult evaluation = ruleEngineService.evaluate(txn);

        Alert alert = null;
        if (evaluation.hasTriggeredRules()) {
            Alert newAlert = new Alert();
            newAlert.setStatus(STATUS_OPEN);
            newAlert.setSeverityScore(evaluation.getSeverityScore());
            newAlert.setTxnId(txn.getTxnId());
            newAlert.setAlertTimestamp(LocalDateTime.now());
            newAlert.setRuleIds(Alert.toRuleIdsString(evaluation.getTriggeredRuleIds()));

            Long generatedId = alertRepository.insert(newAlert);
            newAlert.setId(generatedId);
            alert = newAlert;
        }

        return new TransactionIntakeResult(txn, alert, evaluation.getSeverityLevel());
    }

    /**
     * Returns all active alerts (OPEN, ACKNOWLEDGED, INVESTIGATING) enriched
     * with transaction and customer context, for the dashboard queue.
     */
    public List<AlertDetailDTO> getActiveAlerts() {
        List<AlertDetailDTO> alerts = alertRepository.findActiveAlertDetails();
        alerts.forEach(dto -> dto.setSeverityLevel(RuleEngineService.getSeverityLevel(dto.getSeverityScore())));
        return alerts;
    }

    /**
     * Returns the full detail view for a single alert, including its
     * complete audit history.
     */
    public AlertDetailDTO getAlertDetail(Long alertId) {
        AlertDetailDTO dto = alertRepository.findAlertDetailById(alertId)
                .orElseThrow(() -> new NoSuchElementException("Alert not found: " + alertId));
        dto.setSeverityLevel(RuleEngineService.getSeverityLevel(dto.getSeverityScore()));
        dto.setAuditLogs(alertLogRepository.findByAlertId(alertId));
        return dto;
    }

    /**
     * Transitions an alert to a new status, validating the allowed
     * workflow order and the mandatory-comment rule for DISMISSED/CLOSED,
     * then writes an audit log entry for the change.
     *
     * @throws NoSuchElementException   if no alert exists with the given ID
     * @throws IllegalArgumentException if the status is missing, the
     *                                  transition is not allowed, or a
     *                                  required comment is missing/blank
     */
    public AlertDetailDTO updateAlertStatus(Long alertId, AlertStatusUpdateRequest request) {
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        String newStatus = request.getStatus().trim().toUpperCase();

        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new NoSuchElementException("Alert not found: " + alertId));

        validateTransition(alert.getStatus(), newStatus);

        if (COMMENT_REQUIRED_STATUSES.contains(newStatus)
                && (request.getComment() == null || request.getComment().isBlank())) {
            throw new IllegalArgumentException(
                    "A comment is required when setting status to " + newStatus);
        }

        alertRepository.updateStatus(alertId, newStatus);

        AlertLog log = new AlertLog();
        log.setAlertId(alertId);
        log.setComment(request.getComment() == null ? "" : request.getComment());
        log.setStatus(newStatus);
        log.setLogTimestamp(LocalDateTime.now());
        alertLogRepository.insert(log);

        return getAlertDetail(alertId);
    }

    private void validateTransition(String currentStatus, String newStatus) {
        Set<String> allowedNextStatuses = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowedNextStatuses == null || !allowedNextStatuses.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Cannot transition alert from " + currentStatus + " to " + newStatus);
        }
    }

    /**
     * Result of processing an incoming transaction: the persisted
     * transaction plus the alert generated for it, if any rule fired.
     */
    public static class TransactionIntakeResult {
        private final Transaction transaction;
        private final Alert alert;
        private final String severityLevel;

        public TransactionIntakeResult(Transaction transaction, Alert alert, String severityLevel) {
            this.transaction = transaction;
            this.alert = alert;
            this.severityLevel = severityLevel;
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public Alert getAlert() {
            return alert;
        }

        public String getSeverityLevel() {
            return severityLevel;
        }

        public boolean isAlertGenerated() {
            return alert != null;
        }
    }
}
