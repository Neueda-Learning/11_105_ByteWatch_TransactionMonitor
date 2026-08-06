package org.ByteWatch.repository;

import org.ByteWatch.model.Transaction;
import org.ByteWatch.model.TransactionLiveViewDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data access contract for the {@code transactions} table.
 * Kept as an interface so {@code AlertService} depends on this
 * abstraction rather than the JDBC implementation directly, and so
 * it can be mocked cleanly in service-layer unit tests.
 */
public interface TransactionRepository {

    /**
     * Inserts a new transaction record.
     */
    void insert(Transaction txn);

    /**
     * Fetches a single transaction by its ID.
     */
    Optional<Transaction> findByTxnId(String txnId);

    /**
     * Rule 2 (High Velocity) support: counts how many transactions the
     * given payer has made at or after {@code since}.
     */
    int countByPayerSince(String payerAccNum, LocalDateTime since);

    /**
     * Rule 3 (New Payee) support: true if this payer has transacted with
     * this payee before, excluding the transaction currently being
     * evaluated.
     */
    boolean hasPriorTransactionToPayee(String payerAccNum, String payeeAccNum, String excludeTxnId);

    /**
     * Rule 4 (Daily Limit) support: sums the amount of all transactions
     * made by the given payer in the given currency at or after {@code since}.
     */
    BigDecimal sumAmountByPayerAndCurrencySince(String payerAccNum, String currency, LocalDateTime since);

    /**
     * Realtime feed support: fetches latest transactions with an alert flag.
     */
    List<TransactionLiveViewDTO> findRecentTransactions(int limit);

    /**
     * Realtime feed support: fetches latest transactions page with offset.
     */
    List<TransactionLiveViewDTO> findRecentTransactionsPage(int limit, int offset);

    /**
     * Realtime feed support: total transactions available for pagination.
     */
    long countTransactions();
}
