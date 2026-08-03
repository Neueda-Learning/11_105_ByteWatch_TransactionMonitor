package org.ByteWatch.model;

/**
 * Stage 4 (Investigation Workflow): payload to update alert status and
 * capture analyst context.
 */
public class AlertStatusUpdateRequest {

    private String status;
    private String comment;

    public AlertStatusUpdateRequest() {
    }

    public AlertStatusUpdateRequest(String status, String comment) {
        this.status = status;
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
