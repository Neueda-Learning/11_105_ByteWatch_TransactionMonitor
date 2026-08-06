package org.ByteWatch.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Stage 2 (Transaction Ingestion): immutable transaction facts used by
 * rule evaluation and downstream alert creation.
 */
public class Transaction {

    @NotBlank(message = "txnId is required")
    private String txnId;
    @NotNull(message = "timestamp is required")
    private LocalDateTime timestamp;
    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;
    @NotBlank(message = "currency is required")
    @Pattern(regexp = "USD|GBP|INR|EUR", message = "currency must be one of USD, GBP, INR, EUR")
    private String currency;      // USD, GBP, INR, EUR
    @NotBlank(message = "payeeAccNum is required")
    private String payeeAccNum;
    @NotBlank(message = "payerAccNum is required")
    private String payerAccNum;
    private String status;
    private String type;

    public Transaction() {
    }

    public Transaction(String txnId, LocalDateTime timestamp, BigDecimal amount, String currency,
                       String payeeAccNum, String payerAccNum, String status, String type) {
        this.txnId = txnId;
        this.timestamp = timestamp;
        this.amount = amount;
        this.currency = currency;
        this.payeeAccNum = payeeAccNum;
        this.payerAccNum = payerAccNum;
        this.status = status;
        this.type = type;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPayeeAccNum() {
        return payeeAccNum;
    }

    public void setPayeeAccNum(String payeeAccNum) {
        this.payeeAccNum = payeeAccNum;
    }

    public String getPayerAccNum() {
        return payerAccNum;
    }

    public void setPayerAccNum(String payerAccNum) {
        this.payerAccNum = payerAccNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
