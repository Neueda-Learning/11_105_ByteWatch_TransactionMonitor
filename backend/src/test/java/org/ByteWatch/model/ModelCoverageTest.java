package org.ByteWatch.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelCoverageTest {

    @Test
    void customer_alert_and_alertLog_gettersSettersAndConstructors_work() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 6, 10, 0);

        Customer customer = new Customer(1L, "Ava", "ACC-1", "SAVINGS", "Bank", "USD");
        assertEquals(1L, customer.getId());
        assertEquals("Ava", customer.getName());
        assertEquals("ACC-1", customer.getAccNum());
        assertEquals("SAVINGS", customer.getAccType());
        assertEquals("Bank", customer.getBankName());
        assertEquals("USD", customer.getCurrency());

        customer.setName("Sam");
        assertEquals("Sam", customer.getName());

        Alert alert = new Alert(5L, "OPEN", 40, "TXN-5", now, "1,2");
        assertEquals(5L, alert.getId());
        assertEquals("OPEN", alert.getStatus());
        assertEquals(40, alert.getSeverityScore());
        assertEquals("TXN-5", alert.getTxnId());
        assertEquals(now, alert.getAlertTimestamp());
        assertEquals("1,2", alert.getRuleIds());
        assertEquals(List.of(1, 2), alert.getRuleIdsAsList());
        assertEquals("3,4,5", Alert.toRuleIdsString(List.of(3, 4, 5)));

        alert.setRuleIds(" ");
        assertTrue(alert.getRuleIdsAsList().isEmpty());

        AlertLog log = new AlertLog(8L, 5L, "checked", "DISMISSED", now);
        assertEquals(8L, log.getId());
        assertEquals(5L, log.getAlertId());
        assertEquals("checked", log.getComment());
        assertEquals("DISMISSED", log.getStatus());
        assertEquals(now, log.getLogTimestamp());
    }

    @Test
    void alertDetailDto_and_simulationDtos_gettersAndSetters_work() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 6, 11, 0);

        AlertDetailDTO dto = new AlertDetailDTO();
        dto.setAlertId(7L);
        dto.setStatus("ACKNOWLEDGED");
        dto.setSeverityScore(65);
        dto.setSeverityLevel("HIGH");
        dto.setRuleIds(List.of(1, 3));
        dto.setAlertTimestamp(now);
        dto.setTxnId("TXN-7");
        dto.setAmount(new BigDecimal("99.95"));
        dto.setCurrency("EUR");
        dto.setTxnTimestamp(now.minusMinutes(1));
        dto.setPayerAccNum("ACC-10");
        dto.setPayerName("Payer");
        dto.setPayerBankName("PB");
        dto.setPayeeAccNum("ACC-20");
        dto.setPayeeName("Payee");
        dto.setPayeeBankName("QB");
        dto.setAuditLogs(List.of());

        assertEquals(7L, dto.getAlertId());
        assertEquals("ACKNOWLEDGED", dto.getStatus());
        assertEquals(65, dto.getSeverityScore());
        assertEquals("HIGH", dto.getSeverityLevel());
        assertEquals(List.of(1, 3), dto.getRuleIds());
        assertEquals(now, dto.getAlertTimestamp());
        assertEquals("TXN-7", dto.getTxnId());
        assertEquals(new BigDecimal("99.95"), dto.getAmount());
        assertEquals("EUR", dto.getCurrency());
        assertEquals(now.minusMinutes(1), dto.getTxnTimestamp());
        assertEquals("ACC-10", dto.getPayerAccNum());
        assertEquals("Payer", dto.getPayerName());
        assertEquals("PB", dto.getPayerBankName());
        assertEquals("ACC-20", dto.getPayeeAccNum());
        assertEquals("Payee", dto.getPayeeName());
        assertEquals("QB", dto.getPayeeBankName());
        assertTrue(dto.getAuditLogs().isEmpty());

        SimulationStartRequest startRequest = new SimulationStartRequest();
        startRequest.setMinDelayMs(250);
        startRequest.setMaxDelayMs(500);
        startRequest.setBurstChance(0.2);
        startRequest.setSuspiciousChance(0.1);
        assertEquals(250, startRequest.getMinDelayMs());
        assertEquals(500, startRequest.getMaxDelayMs());
        assertEquals(0.2, startRequest.getBurstChance());
        assertEquals(0.1, startRequest.getSuspiciousChance());

        SimulationStatusResponse status = new SimulationStatusResponse();
        status.setRunning(true);
        status.setMinDelayMs(250);
        status.setMaxDelayMs(500);
        status.setBurstChance(0.3);
        status.setSuspiciousChance(0.4);
        status.setGeneratedTransactions(50);
        status.setGeneratedAlerts(9);
        status.setLastTransactionId("SIM-1");
        status.setLastSeverityLevel("HIGH");
        status.setLastGeneratedAt("2026-08-06T11:00:00");
        status.setLastError("none");

        assertTrue(status.isRunning());
        assertEquals(250, status.getMinDelayMs());
        assertEquals(500, status.getMaxDelayMs());
        assertEquals(0.3, status.getBurstChance());
        assertEquals(0.4, status.getSuspiciousChance());
        assertEquals(50, status.getGeneratedTransactions());
        assertEquals(9, status.getGeneratedAlerts());
        assertEquals("SIM-1", status.getLastTransactionId());
        assertEquals("HIGH", status.getLastSeverityLevel());
        assertEquals("2026-08-06T11:00:00", status.getLastGeneratedAt());
        assertEquals("none", status.getLastError());
        assertFalse(status.getLastError().isEmpty());
    }

    @Test
    void transaction_constructor_and_setters_work() {
        LocalDateTime ts = LocalDateTime.of(2026, 8, 6, 12, 0);
        Transaction transaction = new Transaction(
                "TXN-9",
                ts,
                new BigDecimal("123.45"),
                "USD",
                "ACC-22",
                "ACC-11",
                "COMPLETED",
                "TRANSFER");

        assertEquals("TXN-9", transaction.getTxnId());
        assertEquals(ts, transaction.getTimestamp());
        assertEquals(new BigDecimal("123.45"), transaction.getAmount());
        assertEquals("USD", transaction.getCurrency());
        assertEquals("ACC-22", transaction.getPayeeAccNum());
        assertEquals("ACC-11", transaction.getPayerAccNum());
        assertEquals("COMPLETED", transaction.getStatus());
        assertEquals("TRANSFER", transaction.getType());

        transaction.setType("PAYMENT");
        assertEquals("PAYMENT", transaction.getType());
    }
}
