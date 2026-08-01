package org.ByteWatch.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.ByteWatch.model.Alert;
import org.ByteWatch.model.AlertDetailDTO;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC-based implementation of {@link AlertRepository}.
 */
@Repository
public class JdbcAlertRepositoryImpl implements AlertRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAlertRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static List<Integer> parseRuleIds(String ruleIds) {
        if (ruleIds == null || ruleIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(ruleIds.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private static final RowMapper<Alert> ALERT_ROW_MAPPER = (rs, rowNum) -> {
        Alert alert = new Alert();
        alert.setId(rs.getLong("id"));
        alert.setStatus(rs.getString("status"));
        alert.setSeverityScore(rs.getInt("severity_score"));
        alert.setTxnId(rs.getString("txn_id"));
        alert.setAlertTimestamp(rs.getTimestamp("alert_timestamp").toLocalDateTime());
        alert.setRuleIds(rs.getString("rule_ids"));
        return alert;
    };

    /** Maps a joined alert+transaction+customer row into the DTO. Severity level and audit logs are left for the service layer to fill in. */
    private static final RowMapper<AlertDetailDTO> ALERT_DETAIL_ROW_MAPPER = (rs, rowNum) -> {
        AlertDetailDTO dto = new AlertDetailDTO();
        dto.setAlertId(rs.getLong("id"));
        dto.setStatus(rs.getString("status"));
        dto.setSeverityScore(rs.getInt("severity_score"));
        dto.setAlertTimestamp(rs.getTimestamp("alert_timestamp").toLocalDateTime());
        dto.setRuleIds(parseRuleIds(rs.getString("rule_ids")));

        dto.setTxnId(rs.getString("txn_id"));
        dto.setAmount(rs.getBigDecimal("amount"));
        dto.setCurrency(rs.getString("currency"));
        dto.setTxnTimestamp(rs.getTimestamp("txn_timestamp").toLocalDateTime());

        dto.setPayerAccNum(rs.getString("payer_acc_num"));
        dto.setPayerName(rs.getString("payer_name"));
        dto.setPayerBankName(rs.getString("payer_bank_name"));

        dto.setPayeeAccNum(rs.getString("payee_acc_num"));
        dto.setPayeeName(rs.getString("payee_name"));
        dto.setPayeeBankName(rs.getString("payee_bank_name"));

        return dto;
    };

    private static final String DETAIL_JOIN_SQL =
            "SELECT a.id, a.status, a.severity_score, a.txn_id, a.alert_timestamp, a.rule_ids, " +
            "t.amount, t.currency, t.timestamp AS txn_timestamp, " +
            "t.payer_acc_num, t.payee_acc_num, " +
            "payer.name AS payer_name, payer.bank_name AS payer_bank_name, " +
            "payee.name AS payee_name, payee.bank_name AS payee_bank_name " +
            "FROM alerts a " +
            "JOIN transactions t ON a.txn_id = t.txn_id " +
            "LEFT JOIN customers payer ON t.payer_acc_num = payer.acc_num " +
            "LEFT JOIN customers payee ON t.payee_acc_num = payee.acc_num ";

    @Override
    public Long insert(Alert alert) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO alerts (status, severity_score, txn_id, alert_timestamp, rule_ids) " +
                            "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, alert.getStatus());
            ps.setInt(2, alert.getSeverityScore());
            ps.setString(3, alert.getTxnId());
            ps.setTimestamp(4, Timestamp.valueOf(alert.getAlertTimestamp()));
            ps.setString(5, alert.getRuleIds());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Optional<Alert> findById(Long id) {
        String sql = "SELECT * FROM alerts WHERE id = ?";
        List<Alert> results = jdbcTemplate.query(sql, ALERT_ROW_MAPPER, id);
        return results.stream().findFirst();
    }

    @Override
    public void updateStatus(Long id, String newStatus) {
        String sql = "UPDATE alerts SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, newStatus, id);
    }

    @Override
    public List<AlertDetailDTO> findActiveAlertDetails() {
        String sql = DETAIL_JOIN_SQL +
                "WHERE a.status IN ('OPEN', 'ACKNOWLEDGED', 'INVESTIGATING') " +
                "ORDER BY a.alert_timestamp DESC";
        return jdbcTemplate.query(sql, ALERT_DETAIL_ROW_MAPPER);
    }

    @Override
    public Optional<AlertDetailDTO> findAlertDetailById(Long id) {
        String sql = DETAIL_JOIN_SQL + "WHERE a.id = ?";
        try {
            AlertDetailDTO dto = jdbcTemplate.queryForObject(sql, ALERT_DETAIL_ROW_MAPPER, id);
            return Optional.ofNullable(dto);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
