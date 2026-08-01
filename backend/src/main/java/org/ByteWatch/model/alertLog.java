package org.ByteWatch.model;

import java.time.LocalDateTime;

/**
 * Represents a single audit-trail entry recorded every time an
 * alert's status changes, as stored in the {@code alert_log} table.
 */
public class alertLog {

    private Long id;
    private Long alertId;
    private String comment;
    private String status;
    private LocalDateTime logTimestamp;

    public alertLog() {
    }

    public alertLog(Long id, Long alertId, String comment, String status, LocalDateTime logTimestamp) {
        this.id = id;
        this.alertId = alertId;
        this.comment = comment;
        this.status = status;
        this.logTimestamp = logTimestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLogTimestamp() {
        return logTimestamp;
    }

    public void setLogTimestamp(LocalDateTime logTimestamp) {
        this.logTimestamp = logTimestamp;
    }
}
