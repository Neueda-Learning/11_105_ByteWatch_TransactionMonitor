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
import org.ByteWatch.repository.AlertLogRepository;
import org.ByteWatch.repository.AlertRepository;
import org.ByteWatch.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
}
