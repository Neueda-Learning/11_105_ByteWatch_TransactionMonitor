package org.ByteWatch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ByteWatch.model.Alert;
import org.ByteWatch.model.AlertDetailDTO;
import org.ByteWatch.model.AlertLog;
import org.ByteWatch.model.AlertStatusUpdateRequest;
import org.ByteWatch.model.Transaction;
import org.ByteWatch.repository.AlertLogRepository;
import org.ByteWatch.repository.AlertRepository;
import org.ByteWatch.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AlertRepository alertRepository;
    @Mock
    private AlertLogRepository alertLogRepository;
    @Mock
    private RuleEngineService ruleEngineService;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(transactionRepository, alertRepository, alertLogRepository, ruleEngineService);
    }

    private Alert investigatingAlert() {
        Alert alert = new Alert();
        alert.setId(1L);
        alert.setStatus("INVESTIGATING");
        alert.setSeverityScore(70);
        alert.setTxnId("TXN-1");
        alert.setAlertTimestamp(LocalDateTime.now());
        alert.setRuleIds("1,2");
        return alert;
    }

    private AlertDetailDTO alertDetailStub() {
        AlertDetailDTO dto = new AlertDetailDTO();
        dto.setAlertId(1L);
        dto.setStatus("DISMISSED");
        dto.setSeverityScore(70);
        return dto;
    }

    @Test
    void updateAlertStatus_dismissWithoutComment_usesDefaultCommentAndSucceeds() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(investigatingAlert()));
        when(alertRepository.findAlertDetailById(1L)).thenReturn(Optional.of(alertDetailStub()));
        when(alertLogRepository.findByAlertId(1L)).thenReturn(List.of());

        AlertStatusUpdateRequest request = new AlertStatusUpdateRequest("DISMISSED", "   ");

        AlertDetailDTO result = alertService.updateAlertStatus(1L, request);

        verify(alertRepository, times(1)).updateStatus(eq(1L), eq("DISMISSED"));

        ArgumentCaptor<AlertLog> logCaptor = ArgumentCaptor.forClass(AlertLog.class);
        verify(alertLogRepository, times(1)).insert(logCaptor.capture());
        AlertLog capturedLog = logCaptor.getValue();
        assertEquals("No comment added", capturedLog.getComment());
        assertEquals("DISMISSED", result.getStatus());
    }

    @Test
    void updateAlertStatus_invalidTransition_throwsIllegalArgumentException() {
        Alert openAlert = investigatingAlert();
        openAlert.setStatus("OPEN");
        when(alertRepository.findById(1L)).thenReturn(Optional.of(openAlert));

        // OPEN -> DISMISSED skips ACKNOWLEDGED and INVESTIGATING, so it must be rejected.
        AlertStatusUpdateRequest request = new AlertStatusUpdateRequest("DISMISSED", "Trying to skip ahead");

        assertThrows(IllegalArgumentException.class, () -> alertService.updateAlertStatus(1L, request));

        verify(alertRepository, never()).updateStatus(any(), any());
        verify(alertLogRepository, never()).insert(any());
    }

    @Test
    void updateAlertStatus_validDismissWithComment_updatesStatusAndWritesAuditLog() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(investigatingAlert()));
        when(alertRepository.findAlertDetailById(1L)).thenReturn(Optional.of(alertDetailStub()));
        when(alertLogRepository.findByAlertId(1L)).thenReturn(List.of());

        String comment = "Verified legitimate purchase with customer over phone.";
        AlertStatusUpdateRequest request = new AlertStatusUpdateRequest("DISMISSED", comment);

        AlertDetailDTO result = alertService.updateAlertStatus(1L, request);

        verify(alertRepository, times(1)).updateStatus(eq(1L), eq("DISMISSED"));

        ArgumentCaptor<AlertLog> logCaptor = ArgumentCaptor.forClass(AlertLog.class);
        verify(alertLogRepository, times(1)).insert(logCaptor.capture());
        AlertLog capturedLog = logCaptor.getValue();
        assertEquals(1L, capturedLog.getAlertId());
        assertEquals("DISMISSED", capturedLog.getStatus());
        assertEquals(comment, capturedLog.getComment());

        assertEquals("DISMISSED", result.getStatus());
    }

    @Test
    void processTransaction_whenPayloadMissingRequiredFields_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> alertService.processTransaction(new Transaction()));

        assertEquals("txnId and timestamp are required", ex.getMessage());
    }

    @Test
    void processTransaction_withoutStatus_setsDefaultStatusAndCreatesAlertWhenRulesTriggered() {
        Transaction txn = new Transaction();
        txn.setTxnId("TXN-200");
        txn.setTimestamp(LocalDateTime.now());
        txn.setAmount(new BigDecimal("20000"));
        txn.setCurrency("USD");
        txn.setPayerAccNum("ACC-1");
        txn.setPayeeAccNum("ACC-2");
        txn.setType("WIRE");

        RuleEngineService.RuleEvaluationResult evaluation =
                new RuleEngineService.RuleEvaluationResult(List.of(1, 2), 70, "HIGH");

        when(ruleEngineService.evaluate(txn)).thenReturn(evaluation);
        when(alertRepository.insert(any(Alert.class))).thenReturn(99L);
        doNothing().when(transactionRepository).insert(any(Transaction.class));

        AlertService.TransactionIntakeResult result = alertService.processTransaction(txn);

        assertEquals("COMPLETED", txn.getStatus());
        assertTrue(result.isAlertGenerated());
        assertEquals("HIGH", result.getSeverityLevel());
        assertEquals(99L, result.getAlert().getId());
        verify(transactionRepository).insert(txn);
        verify(alertRepository).insert(any(Alert.class));
    }

    @Test
    void processTransaction_withoutTriggeredRules_returnsNoAlert() {
        Transaction txn = new Transaction();
        txn.setTxnId("TXN-201");
        txn.setTimestamp(LocalDateTime.now());
        txn.setAmount(new BigDecimal("50"));
        txn.setCurrency("USD");
        txn.setPayerAccNum("ACC-1");
        txn.setPayeeAccNum("ACC-2");
        txn.setStatus("PENDING");

        RuleEngineService.RuleEvaluationResult evaluation =
                new RuleEngineService.RuleEvaluationResult(List.of(), 0, "LOW");
        when(ruleEngineService.evaluate(txn)).thenReturn(evaluation);

        AlertService.TransactionIntakeResult result = alertService.processTransaction(txn);

        assertFalse(result.isAlertGenerated());
        assertEquals("LOW", result.getSeverityLevel());
        verify(alertRepository, never()).insert(any(Alert.class));
    }

    @Test
    void getActiveAlerts_enrichesSeverityLevel() {
        AlertDetailDTO dto = new AlertDetailDTO();
        dto.setSeverityScore(85);
        when(alertRepository.findActiveAlertDetails()).thenReturn(List.of(dto));

        List<AlertDetailDTO> result = alertService.getActiveAlerts();

        assertEquals(1, result.size());
        assertEquals("HIGH", result.get(0).getSeverityLevel());
    }

    @Test
    void getAlertDetail_returnsDtoWithAuditLogAndSeverity() {
        AlertDetailDTO detail = new AlertDetailDTO();
        detail.setAlertId(3L);
        detail.setSeverityScore(45);

        AlertLog log = new AlertLog();
        log.setAlertId(3L);

        when(alertRepository.findAlertDetailById(3L)).thenReturn(Optional.of(detail));
        when(alertLogRepository.findByAlertId(3L)).thenReturn(List.of(log));

        AlertDetailDTO result = alertService.getAlertDetail(3L);

        assertEquals("MEDIUM", result.getSeverityLevel());
        assertNotNull(result.getAuditLogs());
        assertEquals(1, result.getAuditLogs().size());
    }

    @Test
    void getAlertDetail_whenMissing_throwsNoSuchElementException() {
        when(alertRepository.findAlertDetailById(400L)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> alertService.getAlertDetail(400L));

        assertEquals("Alert not found: 400", ex.getMessage());
    }

    @Test
    void updateAlertStatus_whenRequestMissing_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> alertService.updateAlertStatus(1L, null));

        assertEquals("request body is required", ex.getMessage());
    }
}
