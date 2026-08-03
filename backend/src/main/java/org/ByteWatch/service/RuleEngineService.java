package org.ByteWatch.service;

import org.springframework.stereotype.Service;
import org.ByteWatch.model.Transaction;
import org.ByteWatch.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates an incoming transaction against the four core fraud rules
 * and computes a weighted severity score. Contains no persistence
 * logic of its own beyond the read-only lookups needed to evaluate
 * velocity, new-payee, and daily-limit rules.
 */
@Service
public class RuleEngineService {

    // --- Rule thresholds -------------------------------------------------
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000");
    private static final int VELOCITY_WINDOW_MINUTES = 5;
    private static final int VELOCITY_MAX_TXNS = 3;
    private static final BigDecimal DAILY_LIMIT_THRESHOLD = new BigDecimal("50000");
    private static final int DAILY_WINDOW_HOURS = 24;

    // --- Rule IDs (must match values persisted in alerts.rule_ids) -------
    public static final int RULE_HIGH_AMOUNT_ID = 1;
    public static final int RULE_HIGH_VELOCITY_ID = 2;
    public static final int RULE_NEW_PAYEE_ID = 3;
    public static final int RULE_DAILY_LIMIT_ID = 4;

    // --- Rule weights ------------------------------------------------------
    private static final int WEIGHT_HIGH_AMOUNT = 40;
    private static final int WEIGHT_HIGH_VELOCITY = 30;
    private static final int WEIGHT_NEW_PAYEE = 15;
    private static final int WEIGHT_DAILY_LIMIT = 25;

    // --- Severity tiering thresholds ---------------------------------------
    private static final int HIGH_SEVERITY_THRESHOLD = 60;
    private static final int MEDIUM_SEVERITY_THRESHOLD = 30;

    private final TransactionRepository transactionRepository;

    public RuleEngineService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Evaluates all four rules against the given (already-persisted)
     * transaction and returns the triggered rule IDs plus the resulting
     * severity score and level.
     */
    public RuleEvaluationResult evaluate(Transaction txn) {
        List<Integer> triggeredRuleIds = new ArrayList<>();
        int score = 0;

        // Rule 1 - High Amount
        if (txn.getAmount() != null && txn.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            triggeredRuleIds.add(RULE_HIGH_AMOUNT_ID);
            score += WEIGHT_HIGH_AMOUNT;
        }

        // Rule 2 - High Velocity: more than 3 txns by this payer in the last 5 minutes
        LocalDateTime velocityWindowStart = txn.getTimestamp().minusMinutes(VELOCITY_WINDOW_MINUTES);
        int recentTxnCount = transactionRepository.countByPayerSince(txn.getPayerAccNum(), velocityWindowStart);
        if (recentTxnCount > VELOCITY_MAX_TXNS) {
            triggeredRuleIds.add(RULE_HIGH_VELOCITY_ID);
            score += WEIGHT_HIGH_VELOCITY;
        }

        // Rule 3 - New Payee: payer has never transacted with this payee before
        boolean hasPriorTransaction = transactionRepository.hasPriorTransactionToPayee(
                txn.getPayerAccNum(), txn.getPayeeAccNum(), txn.getTxnId());
        if (!hasPriorTransaction) {
            triggeredRuleIds.add(RULE_NEW_PAYEE_ID);
            score += WEIGHT_NEW_PAYEE;
        }

        // Rule 4 - Daily Limit: payer's total in the last 24 hours exceeds $50,000
        LocalDateTime dailyWindowStart = txn.getTimestamp().minusHours(DAILY_WINDOW_HOURS);
        BigDecimal dailySum = transactionRepository.sumAmountByPayerSince(txn.getPayerAccNum(), dailyWindowStart);
        if (dailySum.compareTo(DAILY_LIMIT_THRESHOLD) > 0) {
            triggeredRuleIds.add(RULE_DAILY_LIMIT_ID);
            score += WEIGHT_DAILY_LIMIT;
        }

        return new RuleEvaluationResult(triggeredRuleIds, score, getSeverityLevel(score));
    }

    /**
     * Maps a severity score to its color-coded tier. Public and static so
     * {@code AlertService} can reuse it when building response DTOs for
     * alerts it didn't just create (e.g. on GET requests).
     */
    public static String getSeverityLevel(int severityScore) {
        if (severityScore >= HIGH_SEVERITY_THRESHOLD) {
            return "HIGH";
        } else if (severityScore >= MEDIUM_SEVERITY_THRESHOLD) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Outcome of evaluating a transaction against all four rules.
     */
    public static class RuleEvaluationResult {
        private final List<Integer> triggeredRuleIds;
        private final int severityScore;
        private final String severityLevel;

        public RuleEvaluationResult(List<Integer> triggeredRuleIds, int severityScore, String severityLevel) {
            this.triggeredRuleIds = triggeredRuleIds;
            this.severityScore = severityScore;
            this.severityLevel = severityLevel;
        }

        public List<Integer> getTriggeredRuleIds() {
            return triggeredRuleIds;
        }

        public int getSeverityScore() {
            return severityScore;
        }

        public String getSeverityLevel() {
            return severityLevel;
        }

        public boolean hasTriggeredRules() {
            return !triggeredRuleIds.isEmpty();
        }
    }
}
