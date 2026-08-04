package org.ByteWatch.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.ByteWatch.model.AlertLog;

import java.sql.Timestamp;
import java.util.List;

/**
 * JDBC-based implementation of {@link AlertLogRepository}.
 */
@Repository
public class JdbcAlertLogRepositoryImpl implements AlertLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAlertLogRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<AlertLog> ALERT_LOG_ROW_MAPPER = (rs, rowNum) -> {
        AlertLog log = new AlertLog();
        log.setId(rs.getLong("id"));
        log.setAlertId(rs.getLong("alert_id"));
        log.setComment(rs.getString("comment"));
        log.setStatus(rs.getString("status"));
        log.setLogTimestamp(rs.getTimestamp("log_timestamp").toLocalDateTime());
        return log;
    };

    @Override
    public void insert(AlertLog log) {
        String sql = "INSERT INTO alert_log (alert_id, comment, status, log_timestamp) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                log.getAlertId(),
                log.getComment(),
                log.getStatus(),
                Timestamp.valueOf(log.getLogTimestamp()));
    }

    @Override
    public List<AlertLog> findByAlertId(Long alertId) {
        String sql = "SELECT * FROM alert_log WHERE alert_id = ? ORDER BY log_timestamp ASC";
        return jdbcTemplate.query(sql, ALERT_LOG_ROW_MAPPER, alertId);
    }
}
