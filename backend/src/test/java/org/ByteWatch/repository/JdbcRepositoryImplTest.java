package org.ByteWatch.repository;

import org.ByteWatch.model.Alert;
import org.ByteWatch.model.AlertDetailDTO;
import org.ByteWatch.model.AlertLog;
import org.ByteWatch.model.Transaction;
import org.ByteWatch.model.TransactionLiveViewDTO;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcRepositoryImplTest {

    @Test
    void jdbcTransactionRepository_coversCrudAndMapperPaths() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcTransactionRepositoryImpl repository = new JdbcTransactionRepositoryImpl(jdbcTemplate);

        LocalDateTime now = LocalDateTime.of(2026, 8, 6, 12, 0);
        Transaction tx = new Transaction("TXN-1", now, new BigDecimal("120.55"), "USD",
                "ACC-2", "ACC-1", "COMPLETED", "TRANSFER");

        repository.insert(tx);
        verify(jdbcTemplate).update(anyString(), any(), any(), any(), any(), any(), any(), any(), any());

        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("txn_id")).thenReturn("TXN-1");
        when(rs.getTimestamp("timestamp")).thenReturn(Timestamp.valueOf(now));
        when(rs.getBigDecimal("amount")).thenReturn(new BigDecimal("120.55"));
        when(rs.getString("currency")).thenReturn("USD");
        when(rs.getString("payee_acc_num")).thenReturn("ACC-2");
        when(rs.getString("payer_acc_num")).thenReturn("ACC-1");
        when(rs.getString("status")).thenReturn("COMPLETED");
        when(rs.getString("type")).thenReturn("TRANSFER");
        when(rs.getBoolean("has_alert")).thenReturn(true);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyString())).thenAnswer(invocation -> {
            RowMapper<Transaction> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(rs, 0));
        });

        Optional<Transaction> found = repository.findByTxnId("TXN-1");
        assertTrue(found.isPresent());
        assertEquals("TXN-1", found.get().getTxnId());

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any())).thenReturn(null);
        assertEquals(0, repository.countByPayerSince("ACC-1", now));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any())).thenReturn(3);
        assertEquals(3, repository.countByPayerSince("ACC-1", now));

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any(), any())).thenReturn(0);
        assertFalse(repository.hasPriorTransactionToPayee("ACC-1", "ACC-2", "TXN-1"));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any(), any())).thenReturn(2);
        assertTrue(repository.hasPriorTransactionToPayee("ACC-1", "ACC-2", "TXN-1"));

        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), any(), any(), any())).thenReturn(null);
        assertEquals(BigDecimal.ZERO, repository.sumAmountByPayerAndCurrencySince("ACC-1", "USD", now));
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), any(), any(), any())).thenReturn(new BigDecimal("500.00"));
        assertEquals(new BigDecimal("500.00"), repository.sumAmountByPayerAndCurrencySince("ACC-1", "USD", now));

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyInt(), anyInt())).thenAnswer(invocation -> {
            RowMapper<TransactionLiveViewDTO> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(rs, 0));
        });

        List<TransactionLiveViewDTO> pageItems = repository.findRecentTransactionsPage(25, 0);
        assertEquals(1, pageItems.size());
        assertEquals("TXN-1", pageItems.get(0).getTxnId());

        List<TransactionLiveViewDTO> latest = repository.findRecentTransactions(10);
        assertEquals(1, latest.size());

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(null);
        assertEquals(0L, repository.countTransactions());
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(11L);
        assertEquals(11L, repository.countTransactions());
    }

    @Test
    void jdbcAlertRepository_coversInsertFindAndDetailPaths() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcAlertRepositoryImpl repository = new JdbcAlertRepositoryImpl(jdbcTemplate);

        LocalDateTime now = LocalDateTime.of(2026, 8, 6, 12, 30);
        Alert alert = new Alert(1L, "OPEN", 40, "TXN-5", now, "1,2");

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);

        doAnswer(invocation -> {
            PreparedStatementCreator creator = invocation.getArgument(0);
            KeyHolder keyHolder = invocation.getArgument(1);
            creator.createPreparedStatement(connection);
            keyHolder.getKeyList().add(Map.of("id", 99L));
            return 1;
        }).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));

        Long generatedId = repository.insert(alert);
        assertEquals(99L, generatedId);
        verify(preparedStatement).setString(1, "OPEN");
        verify(preparedStatement).setInt(2, 40);
        verify(preparedStatement).setString(3, "TXN-5");
        verify(preparedStatement).setTimestamp(anyInt(), any(Timestamp.class));
        verify(preparedStatement).setString(5, "1,2");

        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("status")).thenReturn("OPEN");
        when(rs.getInt("severity_score")).thenReturn(40);
        when(rs.getString("txn_id")).thenReturn("TXN-5");
        when(rs.getTimestamp("alert_timestamp")).thenReturn(Timestamp.valueOf(now));
        when(rs.getString("rule_ids")).thenReturn("1,2");
        when(rs.getBigDecimal("amount")).thenReturn(new BigDecimal("250.00"));
        when(rs.getString("currency")).thenReturn("USD");
        when(rs.getTimestamp("txn_timestamp")).thenReturn(Timestamp.valueOf(now.minusMinutes(2)));
        when(rs.getString("payer_acc_num")).thenReturn("ACC-1");
        when(rs.getString("payer_name")).thenReturn("Payer");
        when(rs.getString("payer_bank_name")).thenReturn("Bank A");
        when(rs.getString("payee_acc_num")).thenReturn("ACC-2");
        when(rs.getString("payee_name")).thenReturn("Payee");
        when(rs.getString("payee_bank_name")).thenReturn("Bank B");

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyLong())).thenAnswer(invocation -> {
            RowMapper<Alert> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(rs, 0));
        });

        Optional<Alert> found = repository.findById(1L);
        assertTrue(found.isPresent());
        assertEquals("OPEN", found.get().getStatus());

        repository.updateStatus(1L, "ACKNOWLEDGED");
        verify(jdbcTemplate).update(eq("UPDATE alerts SET status = ? WHERE id = ?"), eq("ACKNOWLEDGED"), eq(1L));

        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenAnswer(invocation -> {
            RowMapper<AlertDetailDTO> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(rs, 0));
        });

        List<AlertDetailDTO> active = repository.findActiveAlertDetails();
        assertEquals(1, active.size());
        assertEquals(List.of(1, 2), active.get(0).getRuleIds());

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), anyLong())).thenAnswer(invocation -> {
            RowMapper<AlertDetailDTO> mapper = invocation.getArgument(1);
            return mapper.mapRow(rs, 0);
        });

        Optional<AlertDetailDTO> detail = repository.findAlertDetailById(1L);
        assertTrue(detail.isPresent());
        assertEquals("ACC-1", detail.get().getPayerAccNum());

        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), anyLong()))
                .thenThrow(new EmptyResultDataAccessException(1));
        assertTrue(repository.findAlertDetailById(404L).isEmpty());
    }

    @Test
    void jdbcAlertLogRepository_coversInsertAndRowMapper() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcAlertLogRepositoryImpl repository = new JdbcAlertLogRepositoryImpl(jdbcTemplate);

        LocalDateTime now = LocalDateTime.of(2026, 8, 6, 13, 0);
        AlertLog log = new AlertLog(4L, 1L, "checked", "CLOSED", now);

        repository.insert(log);
        verify(jdbcTemplate).update(anyString(), any(), any(), any(), any());

        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(4L);
        when(rs.getLong("alert_id")).thenReturn(1L);
        when(rs.getString("comment")).thenReturn("checked");
        when(rs.getString("status")).thenReturn("CLOSED");
        when(rs.getTimestamp("log_timestamp")).thenReturn(Timestamp.valueOf(now));

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyLong())).thenAnswer(invocation -> {
            RowMapper<AlertLog> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(rs, 0));
        });

        List<AlertLog> logs = repository.findByAlertId(1L);
        assertEquals(1, logs.size());
        assertEquals("checked", logs.get(0).getComment());
    }
}
