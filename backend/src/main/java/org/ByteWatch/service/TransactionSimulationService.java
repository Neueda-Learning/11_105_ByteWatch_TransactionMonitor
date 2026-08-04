package org.ByteWatch.service;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.ByteWatch.model.SimulationStartRequest;
import org.ByteWatch.model.SimulationStatusResponse;
import org.ByteWatch.model.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates transactions over time to exercise the live rule engine and
 * alert creation flow using the same AlertService intake path as the API.
 */
@Service
public class TransactionSimulationService {

    private static final int DEFAULT_MIN_DELAY_MS = 800;
    private static final int DEFAULT_MAX_DELAY_MS = 2800;
    private static final double DEFAULT_BURST_CHANCE = 0.25;
    private static final double DEFAULT_SUSPICIOUS_CHANCE = 0.35;

    private static final List<String> ACCOUNT_POOL = List.of(
            "ACC-1001", "ACC-1002", "ACC-1003", "ACC-1004", "ACC-1005",
            "ACC-1006", "ACC-1007", "ACC-1008", "ACC-1009", "ACC-1010",
            "ACC-1011", "ACC-1012", "ACC-1013", "ACC-1014", "ACC-1015"
    );

    private static final List<String> CURRENCIES = List.of("USD", "GBP", "EUR", "INR");
    private static final List<String> TYPES = List.of("TRANSFER", "PAYMENT", "WIRE");

    private final AlertService alertService;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
    private final AtomicLong txCounter = new AtomicLong(0);
    private final AtomicLong generatedTransactions = new AtomicLong(0);
    private final AtomicLong generatedAlerts = new AtomicLong(0);

    private volatile boolean running = false;
    private volatile int minDelayMs = DEFAULT_MIN_DELAY_MS;
    private volatile int maxDelayMs = DEFAULT_MAX_DELAY_MS;
    private volatile double burstChance = DEFAULT_BURST_CHANCE;
    private volatile double suspiciousChance = DEFAULT_SUSPICIOUS_CHANCE;
    private volatile String lastTransactionId;
    private volatile String lastSeverityLevel;
    private volatile String lastGeneratedAt;
    private volatile String lastError;

    public TransactionSimulationService(AlertService alertService) {
        this.alertService = alertService;
    }

    public synchronized SimulationStatusResponse start(SimulationStartRequest request) {
        if (request != null) {
            applyRequest(request);
        }
        if (!running) {
            running = true;
            lastError = null;
            scheduleNextTick();
        }
        return currentStatus();
    }

    public synchronized SimulationStatusResponse stop() {
        running = false;
        return currentStatus();
    }

    public SimulationStatusResponse currentStatus() {
        SimulationStatusResponse response = new SimulationStatusResponse();
        response.setRunning(running);
        response.setMinDelayMs(minDelayMs);
        response.setMaxDelayMs(maxDelayMs);
        response.setBurstChance(burstChance);
        response.setSuspiciousChance(suspiciousChance);
        response.setGeneratedTransactions(generatedTransactions.get());
        response.setGeneratedAlerts(generatedAlerts.get());
        response.setLastTransactionId(lastTransactionId);
        response.setLastSeverityLevel(lastSeverityLevel);
        response.setLastGeneratedAt(lastGeneratedAt);
        response.setLastError(lastError);
        return response;
    }

    private void applyRequest(SimulationStartRequest request) {
        int proposedMin = request.getMinDelayMs() == null ? minDelayMs : request.getMinDelayMs();
        int proposedMax = request.getMaxDelayMs() == null ? maxDelayMs : request.getMaxDelayMs();

        if (proposedMin < 200 || proposedMax < 200 || proposedMin > proposedMax) {
            throw new IllegalArgumentException("minDelayMs and maxDelayMs must be >= 200 and min <= max");
        }

        double proposedBurstChance = request.getBurstChance() == null ? burstChance : request.getBurstChance();
        double proposedSuspiciousChance = request.getSuspiciousChance() == null ? suspiciousChance : request.getSuspiciousChance();

        if (proposedBurstChance < 0 || proposedBurstChance > 1 || proposedSuspiciousChance < 0 || proposedSuspiciousChance > 1) {
            throw new IllegalArgumentException("burstChance and suspiciousChance must be between 0 and 1");
        }

        minDelayMs = proposedMin;
        maxDelayMs = proposedMax;
        burstChance = proposedBurstChance;
        suspiciousChance = proposedSuspiciousChance;
    }

    private void scheduleNextTick() {
        if (!running) {
            return;
        }
        int delay = randomInt(minDelayMs, maxDelayMs);
        executor.schedule(() -> {
            try {
                emitTransactions();
            } finally {
                scheduleNextTick();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void emitTransactions() {
        if (!running) {
            return;
        }

        boolean burst = randomDouble() < burstChance;
        int count = burst ? randomInt(2, 4) : 1;
        String burstPayer = burst ? randomPayer() : null;

        for (int i = 0; i < count; i++) {
            Transaction tx = buildTransaction(burstPayer);
            try {
                AlertService.TransactionIntakeResult result = alertService.processTransaction(tx);
                generatedTransactions.incrementAndGet();
                if (result.isAlertGenerated()) {
                    generatedAlerts.incrementAndGet();
                }
                lastTransactionId = tx.getTxnId();
                lastSeverityLevel = result.getSeverityLevel();
                lastGeneratedAt = LocalDateTime.now().toString();
                lastError = null;
            } catch (RuntimeException ex) {
                lastError = ex.getMessage();
            }
        }
    }

    private Transaction buildTransaction(String forcedPayer) {
        String payer = forcedPayer == null ? randomPayer() : forcedPayer;
        String payee = randomDifferentAccount(payer);

        boolean suspicious = randomDouble() < suspiciousChance;
        BigDecimal amount = suspicious
                ? randomAmount(11000.00, 75000.00)
                : randomAmount(40.00, 9000.00);

        Transaction tx = new Transaction();
        tx.setTxnId("SIM-" + System.currentTimeMillis() + "-" + txCounter.incrementAndGet());
        tx.setTimestamp(LocalDateTime.now());
        tx.setAmount(amount);
        tx.setCurrency(randomFrom(CURRENCIES));
        tx.setPayeeAccNum(payee);
        tx.setPayerAccNum(payer);
        tx.setStatus(suspicious ? "PENDING" : "COMPLETED");
        tx.setType(randomFrom(TYPES));
        return tx;
    }

    private String randomPayer() {
        return randomFrom(ACCOUNT_POOL);
    }

    private String randomDifferentAccount(String source) {
        String candidate = source;
        while (candidate.equals(source)) {
            candidate = randomFrom(ACCOUNT_POOL);
        }
        return candidate;
    }

    private BigDecimal randomAmount(double min, double max) {
        double value = min + ((max - min) * randomDouble());
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static int randomInt(int min, int max) {
        return min + (int) Math.floor(Math.random() * ((max - min) + 1));
    }

    private static double randomDouble() {
        return Math.random();
    }

    private static <T> T randomFrom(List<T> values) {
        return values.get(randomInt(0, values.size() - 1));
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        executor.shutdownNow();
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "tx-simulator");
            thread.setDaemon(true);
            return thread;
        }
    }
}
