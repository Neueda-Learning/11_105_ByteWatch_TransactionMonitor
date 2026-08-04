package org.ByteWatch.model;

/**
 * Optional runtime controls for transaction simulation.
 */
public class SimulationStartRequest {

    private Integer minDelayMs;
    private Integer maxDelayMs;
    private Double burstChance;
    private Double suspiciousChance;

    public Integer getMinDelayMs() {
        return minDelayMs;
    }

    public void setMinDelayMs(Integer minDelayMs) {
        this.minDelayMs = minDelayMs;
    }

    public Integer getMaxDelayMs() {
        return maxDelayMs;
    }

    public void setMaxDelayMs(Integer maxDelayMs) {
        this.maxDelayMs = maxDelayMs;
    }

    public Double getBurstChance() {
        return burstChance;
    }

    public void setBurstChance(Double burstChance) {
        this.burstChance = burstChance;
    }

    public Double getSuspiciousChance() {
        return suspiciousChance;
    }

    public void setSuspiciousChance(Double suspiciousChance) {
        this.suspiciousChance = suspiciousChance;
    }
}
