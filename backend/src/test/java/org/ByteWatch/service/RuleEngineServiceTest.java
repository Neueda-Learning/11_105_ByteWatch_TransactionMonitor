package org.ByteWatch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ByteWatch.model.Transaction;
import org.ByteWatch.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEngineServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private RuleEngineService ruleEngineService;

    @BeforeEach
    void setUp() {
        ruleEngineService = new RuleEngineService(transactionRepository);
    }

    /**
     * Builds a transaction with no other rules triggered (low velocity,
     * an existing payee relationship, and a low running daily total) so
     * each test can isolate the rule it's exercising.
     */
    private Transaction baselineTransaction(BigDecimal amount) {
        Transaction txn = new Transaction();
        txn.setTxnId("TXN-1");
        txn.setTimestamp(LocalDateTime.now());
        txn.setAmount(amount);
        txn.setCurrency("USD");
        txn.setPayerAccNum("PAYER-1");
        txn.setPayeeAccNum("PAYEE-1");
        txn.setStatus("COMPLETED");
        txn.setType("TRANSFER");
        return txn;
    }

    @Test
    void highAmountRule_triggersWhenAmountExceedsThreshold() {
        Transaction txn = baselineTransaction(new BigDecimal("15000.00"));
        when(transactionRepository.countByPayerSince(anyString(), any())).thenReturn(0);
        when(transactionRepository.hasPriorTransactionToPayee(anyString(), anyString(), anyString())).thenReturn(true);
        when(transactionRepository.sumAmountByPayerSince(anyString(), any())).thenReturn(new BigDecimal("15000.00"));

        RuleEngineService.RuleEvaluationResult result = ruleEngineService.evaluate(txn);

        assertTrue(result.getTriggeredRuleIds().contains(RuleEngineService.RULE_HIGH_AMOUNT_ID));
        assertEquals(40, result.getSeverityScore());
        assertEquals("MEDIUM", result.getSeverityLevel());
    }

    @Test
    void highAmountRule_doesNotTriggerAtOrBelowThreshold() {
        Transaction txn = baselineTransaction(new BigDecimal("10000.00"));
        when(transactionRepository.countByPayerSince(anyString(), any())).thenReturn(0);
        when(transactionRepository.hasPriorTransactionToPayee(anyString(), anyString(), anyString())).thenReturn(true);
        when(transactionRepository.sumAmountByPayerSince(anyString(), any())).thenReturn(new BigDecimal("10000.00"));

        RuleEngineService.RuleEvaluationResult result = ruleEngineService.evaluate(txn);

        assertFalse(result.getTriggeredRuleIds().contains(RuleEngineService.RULE_HIGH_AMOUNT_ID));
        assertEquals(0, result.getSeverityScore());
        assertEquals("LOW", result.getSeverityLevel());
    }

    @Test
    void multipleRulesTriggered_sumToHighSeverity() {
        // Amount (40) + Velocity (30) + New Payee (15) = 85 -> HIGH
        Transaction txn = baselineTransaction(new BigDecimal("20000.00"));
        when(transactionRepository.countByPayerSince(anyString(), any())).thenReturn(5);
        when(transactionRepository.hasPriorTransactionToPayee(anyString(), anyString(), anyString())).thenReturn(false);
        when(transactionRepository.sumAmountByPayerSince(anyString(), any())).thenReturn(new BigDecimal("20000.00"));

        RuleEngineService.RuleEvaluationResult result = ruleEngineService.evaluate(txn);

        assertEquals(85, result.getSeverityScore());
        assertEquals("HIGH", result.getSeverityLevel());
        assertTrue(result.getTriggeredRuleIds().containsAll(
                java.util.List.of(RuleEngineService.RULE_HIGH_AMOUNT_ID,
                        RuleEngineService.RULE_HIGH_VELOCITY_ID,
                        RuleEngineService.RULE_NEW_PAYEE_ID)));
    }
}
