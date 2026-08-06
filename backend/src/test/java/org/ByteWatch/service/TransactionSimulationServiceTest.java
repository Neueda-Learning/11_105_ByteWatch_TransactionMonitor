package org.ByteWatch.service;

import org.ByteWatch.model.Alert;
import org.ByteWatch.model.SimulationStartRequest;
import org.ByteWatch.model.SimulationStatusResponse;
import org.ByteWatch.model.Transaction;
import org.ByteWatch.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionSimulationServiceTest {

    private AlertService alertService;
    private TransactionRepository transactionRepository;
    private TransactionSimulationService simulationService;

    @BeforeEach
    void setUp() {
        alertService = mock(AlertService.class);
        transactionRepository = mock(TransactionRepository.class);
        simulationService = new TransactionSimulationService(alertService, transactionRepository);
    }

    @AfterEach
    void tearDown() {
        simulationService.shutdown();
    }

    @Test
    void currentStatus_returnsDefaultsBeforeStart() {
        SimulationStatusResponse status = simulationService.currentStatus();

        assertFalse(status.isRunning());
        assertEquals(800, status.getMinDelayMs());
        assertEquals(2800, status.getMaxDelayMs());
        assertEquals(0.25, status.getBurstChance());
        assertEquals(0.15, status.getSuspiciousChance());
        assertEquals(0, status.getGeneratedTransactions());
        assertEquals(0, status.getGeneratedAlerts());
    }

    @Test
    void start_rejectsInvalidDelayWindow() {
        SimulationStartRequest request = new SimulationStartRequest();
        request.setMinDelayMs(100);
        request.setMaxDelayMs(150);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> simulationService.start(request));

        assertEquals("minDelayMs and maxDelayMs must be >= 200 and min <= max", ex.getMessage());
    }

    @Test
    void start_rejectsInvalidChances() {
        SimulationStartRequest request = new SimulationStartRequest();
        request.setBurstChance(1.2);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> simulationService.start(request));

        assertEquals("burstChance and suspiciousChance must be between 0 and 1", ex.getMessage());
    }

    @Test
    void emitTransactions_recordsAlertOutcome_whenSuspiciousAlwaysTrue() throws Exception {
        setField("running", true);
        setField("suspiciousChance", 1.0d);

        Transaction source = new Transaction();
        source.setTxnId("SIM-1");
        source.setTimestamp(LocalDateTime.now());
        Alert generatedAlert = new Alert();
        generatedAlert.setId(10L);

        when(alertService.processTransaction(any(Transaction.class)))
                .thenReturn(new AlertService.TransactionIntakeResult(source, generatedAlert, "HIGH"));

        invokePrivate("emitTransactions");

        SimulationStatusResponse status = simulationService.currentStatus();
        assertEquals(1, status.getGeneratedTransactions());
        assertEquals(1, status.getGeneratedAlerts());
        assertEquals("HIGH", status.getLastSeverityLevel());
        assertNotNull(status.getLastTransactionId());
        assertNotNull(status.getLastGeneratedAt());
    }

    @Test
    void emitTransactions_setsLastError_whenProcessingFails() throws Exception {
        setField("running", true);
        setField("suspiciousChance", 1.0d);

        when(alertService.processTransaction(any(Transaction.class)))
                .thenThrow(new RuntimeException("sim failure"));

        invokePrivate("emitTransactions");

        SimulationStatusResponse status = simulationService.currentStatus();
        assertEquals("sim failure", status.getLastError());
    }

    @Test
    void buildBenignTransaction_fallsBackToKnownPair_whenNoPriorRelationshipFound() throws Exception {
        when(transactionRepository.hasPriorTransactionToPayee(anyString(), anyString(), anyString()))
                .thenReturn(false);

        Transaction transaction = (Transaction) invokePrivate("buildBenignTransaction");

        assertEquals("ACC-1005", transaction.getPayerAccNum());
        assertEquals("ACC-1002", transaction.getPayeeAccNum());
        assertEquals("COMPLETED", transaction.getStatus());
        assertNotNull(transaction.getTimestamp());
    }

    @Test
    void helperMethods_coverDefaultThreshold_and_shutdownStopsService() throws Exception {
        BigDecimal fallback = (BigDecimal) invokePrivate("highAmountThreshold", "XYZ");
        assertEquals(new BigDecimal("10000"), fallback);

        setField("running", true);
        simulationService.shutdown();

        assertFalse(simulationService.currentStatus().isRunning());
    }

    @Test
    void stop_returnsNotRunning() {
        SimulationStatusResponse status = simulationService.stop();
        assertFalse(status.isRunning());
    }

    private Object invokePrivate(String methodName, Object... args) throws Exception {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        Method method = TransactionSimulationService.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(simulationService, args);
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = TransactionSimulationService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(simulationService, value);
    }
}
