package org.ByteWatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ByteWatch.model.Alert;
import org.ByteWatch.model.Transaction;
import org.ByteWatch.model.TransactionLivePageResponse;
import org.ByteWatch.model.TransactionLiveViewDTO;
import org.ByteWatch.service.AlertService;
import org.ByteWatch.service.TransactionFeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        alertService = mock(AlertService.class);
        transactionFeedService = mock(TransactionFeedService.class);
        objectMapper = new ObjectMapper().findAndRegisterModules();
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TransactionController(alertService, transactionFeedService))
                .setValidator(validator)
                .build();
    }

    private Transaction buildValidTransaction() {
        Transaction txn = new Transaction();
        txn.setTxnId("TXN-INT-001");
        txn.setTimestamp(LocalDateTime.of(2026, 8, 3, 18, 10));
        txn.setAmount(new BigDecimal("15000.00"));
        txn.setCurrency("USD");
        txn.setPayeeAccNum("ACC-2001");
        txn.setPayerAccNum("ACC-1001");
        txn.setStatus("COMPLETED");
        txn.setType("TRANSFER");
        return txn;
    }

    @Test
    void submitTransaction_returns201AndIntakeResult() throws Exception {
        Transaction requestTxn = buildValidTransaction();

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

    @Test
    void submitTransaction_withInvalidPayload_returns400WithFieldErrors() throws Exception {
        String invalidPayload = """
                {
                  "txnId": "",
                  "amount": -500,
                  "currency": "XYZ",
                  "payeeAccNum": "",
                  "payerAccNum": "",
                  "status": "COMPLETED",
                  "type": "TRANSFER"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.txnId").exists())
                .andExpect(jsonPath("$.fieldErrors.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors.amount").exists())
                .andExpect(jsonPath("$.fieldErrors.currency").exists())
                .andExpect(jsonPath("$.fieldErrors.payeeAccNum").exists())
                .andExpect(jsonPath("$.fieldErrors.payerAccNum").exists());
    }

    @Test
    void submitTransaction_withDuplicateTxnId_returns409() throws Exception {
        Transaction requestTxn = buildValidTransaction();
        when(alertService.processTransaction(any(Transaction.class)))
                .thenThrow(new DuplicateKeyException("duplicate txn id"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestTxn)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Transaction with the same txnId already exists"));
    }

    @Test
    void getLiveTransactions_returnsPaginatedPayload() throws Exception {
        TransactionLiveViewDTO row = new TransactionLiveViewDTO();
        row.setTxnId("TXN-LIVE-001");
        row.setTimestamp(LocalDateTime.of(2026, 8, 3, 18, 25));
        row.setAmount(new BigDecimal("250.00"));
        row.setCurrency("USD");
        row.setHasAlert(true);

        TransactionLivePageResponse page = new TransactionLivePageResponse();
        page.setItems(List.of(row));
        page.setPage(2);
        page.setPageSize(25);
        page.setTotalItems(101);
        page.setTotalPages(5);
        page.setHasPrevious(true);
        page.setHasNext(true);

        when(transactionFeedService.getRecentTransactionsPage(eq(2), eq(25))).thenReturn(page);

        mockMvc.perform(get("/api/transactions/live")
                        .param("page", "2")
                        .param("pageSize", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.pageSize").value(25))
                .andExpect(jsonPath("$.totalItems").value(101))
                .andExpect(jsonPath("$.totalPages").value(5))
                .andExpect(jsonPath("$.hasPrevious").value(true))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.items[0].txnId").value("TXN-LIVE-001"))
                .andExpect(jsonPath("$.items[0].hasAlert").value(true));
    }

        @Test
        void getLiveTransactions_whenServiceRejectsInput_returns400() throws Exception {
                when(transactionFeedService.getRecentTransactionsPage(eq(0), eq(15)))
                                .thenThrow(new IllegalArgumentException("page must be >= 1"));

                mockMvc.perform(get("/api/transactions/live")
                                                .param("page", "0")
                                                .param("pageSize", "15"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("page must be >= 1"));
        }
}
