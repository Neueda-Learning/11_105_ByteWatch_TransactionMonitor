package org.ByteWatch.model;

import java.util.List;

/**
 * Paginated wrapper for realtime transaction feed polling.
 */
public class TransactionLivePageResponse {

    private List<TransactionLiveViewDTO> items;
    private int page;
    private int pageSize;
    private long totalItems;
    private int totalPages;
    private boolean hasPrevious;
    private boolean hasNext;

    public List<TransactionLiveViewDTO> getItems() {
        return items;
    }

    public void setItems(List<TransactionLiveViewDTO> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
}
