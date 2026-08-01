package org.ByteWatch.model;

/**
 * Request body for {@code PUT /api/alerts/{id}/status}.
 * Example: {"status": "DISMISSED", "comment": "Verified legitimate purchase with customer over phone."}
 */
public class alertStatusUpdateRequest {

    private String status;
    private String comment;

    public alertStatusUpdateRequest() {
    }

    public alertStatusUpdateRequest(String status, String comment) {
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
