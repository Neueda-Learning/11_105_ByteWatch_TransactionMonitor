package org.ByteWatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ByteWatch.model.Alert;
import org.ByteWatch.model.Transaction;
import org.ByteWatch.service.AlertService;
import org.ByteWatch.service.TransactionFeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Endpoint-level verification for transaction intake request/response behavior.
 */
class TransactionControllerIntegrationTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private AlertService alertService;
    private TransactionFeedService transactionFeedService;

    @BeforeEach
    void setUp() {
        alertService = mock(AlertService.class);
        transactionFeedService = mock(TransactionFeedService.class);
        objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(new TransactionController(alertService, transactionFeedService)).build();
    }

    @Test
    void submitTransaction_returns201AndIntakeResult() throws Exception {
        Transaction requestTxn = new Transaction();
        requestTxn.setTxnId("TXN-INT-001");
        requestTxn.setTimestamp(LocalDateTime.of(2026, 8, 3, 18, 10));
        requestTxn.setAmount(new BigDecimal("15000.00"));
        requestTxn.setCurrency("USD");
        requestTxn.setPayeeAccNum("ACC-2001");
        requestTxn.setPayerAccNum("ACC-1001");
        requestTxn.setStatus("COMPLETED");
        requestTxn.setType("TRANSFER");

        Alert alert = new Alert();
        alert.setId(101L);
        alert.setStatus("OPEN");
        alert.setSeverityScore(40);
        alert.setTxnId("TXN-INT-001");
        alert.setAlertTimestamp(LocalDateTime.of(2026, 8, 3, 18, 10));
        alert.setRuleIds("1");

        AlertService.TransactionIntakeResult result =
                new AlertService.TransactionIntakeResult(requestTxn, alert, "MEDIUM");
        when(alertService.processTransaction(any(Transaction.class))).thenReturn(result);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestTxn)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.severityLevel").value("MEDIUM"))
                .andExpect(jsonPath("$.transaction.txnId").value("TXN-INT-001"))
                .andExpect(jsonPath("$.alert.id").value(101));
    }
}
