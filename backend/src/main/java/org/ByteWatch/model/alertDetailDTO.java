package org.ByteWatch.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregated view of an alert used by both {@code GET /api/alerts}
 * (list) and {@code GET /api/alerts/{id}} (single, with full audit
 * history). Combines the alert itself with its triggering
 * transaction and the payer/payee customer context the Risk Manager
 * needs to investigate without extra lookups.
 */
public class alertDetailDTO {

    private Long alertId;
    private String status;
    private int severityScore;
    private String severityLevel;   // HIGH, MEDIUM, LOW
    private List<Integer> ruleIds;
    private LocalDateTime alertTimestamp;

    // Transaction context
    private String txnId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime txnTimestamp;

    // Payer context
    private String payerAccNum;
    private String payerName;
    private String payerBankName;

    // Payee context
    private String payeeAccNum;
    private String payeeName;
    private String payeeBankName;

    // Full audit history (populated for the single-alert detail endpoint)
    private List<alertLog> auditLogs;

    public alertDetailDTO() {
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSeverityScore() {
        return severityScore;
    }

    public void setSeverityScore(int severityScore) {
        this.severityScore = severityScore;
    }

    public String getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(String severityLevel) {
        this.severityLevel = severityLevel;
    }

    public List<Integer> getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(List<Integer> ruleIds) {
        this.ruleIds = ruleIds;
    }

    public LocalDateTime getAlertTimestamp() {
        return alertTimestamp;
    }

    public void setAlertTimestamp(LocalDateTime alertTimestamp) {
        this.alertTimestamp = alertTimestamp;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
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

    public LocalDateTime getTxnTimestamp() {
        return txnTimestamp;
    }

    public void setTxnTimestamp(LocalDateTime txnTimestamp) {
        this.txnTimestamp = txnTimestamp;
    }

    public String getPayerAccNum() {
        return payerAccNum;
    }

    public void setPayerAccNum(String payerAccNum) {
        this.payerAccNum = payerAccNum;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getPayerBankName() {
        return payerBankName;
    }

    public void setPayerBankName(String payerBankName) {
        this.payerBankName = payerBankName;
    }

    public String getPayeeAccNum() {
        return payeeAccNum;
    }

    public void setPayeeAccNum(String payeeAccNum) {
        this.payeeAccNum = payeeAccNum;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public String getPayeeBankName() {
        return payeeBankName;
    }

    public void setPayeeBankName(String payeeBankName) {
        this.payeeBankName = payeeBankName;
    }

    public List<alertLog> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(List<alertLog> auditLogs) {
        this.auditLogs = auditLogs;
    }
}
