package org.ByteWatch.repository;

import org.ByteWatch.model.AlertLog;

import java.util.List;

/**
 * Data access contract for the {@code alert_log} audit trail table.
 * Every alert status transition writes exactly one row here.
 */
public interface AlertLogRepository {

    /**
     * Inserts a new audit-trail entry for an alert status change.
     */
    void insert(AlertLog log);

    /**
     * Returns the full audit history for an alert, oldest first, so the
     * detail view can render it as a timeline.
     */
    List<AlertLog> findByAlertId(Long alertId);
}
