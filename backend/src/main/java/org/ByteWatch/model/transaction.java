package org.ByteWatch.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a single financial transaction as stored in the
 * {@code transactions} table.
 */
public class transaction {

    private String txnId;
    private LocalDateTime timestamp;
    private BigDecimal amount;
    private String currency;      // USD, GBP, INR, EUR
    private String payeeAccNum;
    private String payerAccNum;
    private String status;
    private String type;

    public transaction() {
    }

    public transaction(String txnId, LocalDateTime timestamp, BigDecimal amount, String currency,
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
