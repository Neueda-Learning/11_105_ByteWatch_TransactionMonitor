package org.ByteWatch.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.ByteWatch.model.Transaction;
import org.ByteWatch.model.TransactionLiveViewDTO;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-based implementation of {@link TransactionRepository}.
 * All queries are parameterized to prevent SQL injection.
 */
@Repository
public class JdbcTransactionRepositoryImpl implements TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransactionRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Transaction> TRANSACTION_ROW_MAPPER = (rs, rowNum) -> {
        Transaction txn = new Transaction();
        txn.setTxnId(rs.getString("txn_id"));
        txn.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        txn.setAmount(rs.getBigDecimal("amount"));
        txn.setCurrency(rs.getString("currency"));
        txn.setPayeeAccNum(rs.getString("payee_acc_num"));
        txn.setPayerAccNum(rs.getString("payer_acc_num"));
        txn.setStatus(rs.getString("status"));
        txn.setType(rs.getString("type"));
        return txn;
    };

    private static final RowMapper<TransactionLiveViewDTO> TRANSACTION_LIVE_VIEW_ROW_MAPPER = (rs, rowNum) -> {
        TransactionLiveViewDTO dto = new TransactionLiveViewDTO();
        dto.setTxnId(rs.getString("txn_id"));
        dto.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        dto.setAmount(rs.getBigDecimal("amount"));
        dto.setCurrency(rs.getString("currency"));
        dto.setPayeeAccNum(rs.getString("payee_acc_num"));
        dto.setPayerAccNum(rs.getString("payer_acc_num"));
        dto.setStatus(rs.getString("status"));
        dto.setType(rs.getString("type"));
        dto.setHasAlert(rs.getBoolean("has_alert"));
        return dto;
    };

    @Override
    public void insert(Transaction txn) {
        String sql = "INSERT INTO transactions " +
                "(txn_id, timestamp, amount, currency, payee_acc_num, payer_acc_num, status, type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                txn.getTxnId(),
                Timestamp.valueOf(txn.getTimestamp()),
                txn.getAmount(),
                txn.getCurrency(),
                txn.getPayeeAccNum(),
                txn.getPayerAccNum(),
                txn.getStatus(),
                txn.getType());
    }

    @Override
    public Optional<Transaction> findByTxnId(String txnId) {
        String sql = "SELECT * FROM transactions WHERE txn_id = ?";
        List<Transaction> results = jdbcTemplate.query(sql, TRANSACTION_ROW_MAPPER, txnId);
        return results.stream().findFirst();
    }

    @Override
    public int countByPayerSince(String payerAccNum, LocalDateTime since) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE payer_acc_num = ? AND timestamp >= ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, payerAccNum, Timestamp.valueOf(since));
        return count == null ? 0 : count;
    }

    @Override
    public boolean hasPriorTransactionToPayee(String payerAccNum, String payeeAccNum, String excludeTxnId) {
        String sql = "SELECT COUNT(*) FROM transactions " +
                "WHERE payer_acc_num = ? AND payee_acc_num = ? AND txn_id <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, payerAccNum, payeeAccNum, excludeTxnId);
        return count != null && count > 0;
    }

    @Override
    public BigDecimal sumAmountByPayerAndCurrencySince(String payerAccNum, String currency, LocalDateTime since) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                "WHERE payer_acc_num = ? AND currency = ? AND timestamp >= ?";
        BigDecimal sum = jdbcTemplate.queryForObject(
                sql,
                BigDecimal.class,
                payerAccNum,
                currency,
                Timestamp.valueOf(since));
        return sum == null ? BigDecimal.ZERO : sum;
    }

    @Override
    public List<TransactionLiveViewDTO> findRecentTransactions(int limit) {
        String sql = "SELECT t.txn_id, t.timestamp, t.amount, t.currency, t.payee_acc_num, t.payer_acc_num, t.status, t.type, " +
                "EXISTS (SELECT 1 FROM alerts a WHERE a.txn_id = t.txn_id) AS has_alert " +
                "FROM transactions t " +
                "ORDER BY t.timestamp DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, TRANSACTION_LIVE_VIEW_ROW_MAPPER, limit);
    }
}
