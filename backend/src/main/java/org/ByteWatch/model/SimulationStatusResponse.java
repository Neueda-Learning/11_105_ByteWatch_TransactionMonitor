package org.ByteWatch.model;

/**
 * Current state and counters for the live transaction simulator.
 */
public class SimulationStatusResponse {

    private boolean running;
    private int minDelayMs;
    private int maxDelayMs;
    private double burstChance;
    private double suspiciousChance;
    private long generatedTransactions;
    private long generatedAlerts;
    private String lastTransactionId;
    private String lastSeverityLevel;
    private String lastGeneratedAt;
    private String lastError;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getMinDelayMs() {
        return minDelayMs;
    }

    public void setMinDelayMs(int minDelayMs) {
        this.minDelayMs = minDelayMs;
    }

    public int getMaxDelayMs() {
        return maxDelayMs;
    }

    public void setMaxDelayMs(int maxDelayMs) {
        this.maxDelayMs = maxDelayMs;
    }

    public double getBurstChance() {
        return burstChance;
    }

    public void setBurstChance(double burstChance) {
        this.burstChance = burstChance;
    }

    public double getSuspiciousChance() {
        return suspiciousChance;
    }

    public void setSuspiciousChance(double suspiciousChance) {
        this.suspiciousChance = suspiciousChance;
    }

    public long getGeneratedTransactions() {
        return generatedTransactions;
    }

    public void setGeneratedTransactions(long generatedTransactions) {
        this.generatedTransactions = generatedTransactions;
    }

    public long getGeneratedAlerts() {
        return generatedAlerts;
    }

    public void setGeneratedAlerts(long generatedAlerts) {
        this.generatedAlerts = generatedAlerts;
    }

    public String getLastTransactionId() {
        return lastTransactionId;
    }

    public void setLastTransactionId(String lastTransactionId) {
        this.lastTransactionId = lastTransactionId;
    }

    public String getLastSeverityLevel() {
        return lastSeverityLevel;
    }

    public void setLastSeverityLevel(String lastSeverityLevel) {
        this.lastSeverityLevel = lastSeverityLevel;
    }

    public String getLastGeneratedAt() {
        return lastGeneratedAt;
    }

    public void setLastGeneratedAt(String lastGeneratedAt) {
        this.lastGeneratedAt = lastGeneratedAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
}
