package org.ByteWatch.service;

import org.springframework.stereotype.Service;
import org.ByteWatch.model.TransactionLivePageResponse;
import org.ByteWatch.model.TransactionLiveViewDTO;
import org.ByteWatch.repository.TransactionRepository;

import java.util.Set;
import java.util.List;

/**
 * Read-only service for realtime transaction feed consumption.
 */
@Service
public class TransactionFeedService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 40;
    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(25, 40, 60);

    private final TransactionRepository transactionRepository;

    public TransactionFeedService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionLivePageResponse getRecentTransactionsPage(Integer requestedPage, Integer requestedPageSize) {
        int page = sanitizePage(requestedPage);
        int pageSize = sanitizePageSize(requestedPageSize);

        long totalItems = transactionRepository.countTransactions();
        int totalPages = totalItems == 0
                ? 1
                : (int) Math.ceil((double) totalItems / pageSize);

        int effectivePage = Math.min(page, totalPages);
        int offset = (effectivePage - 1) * pageSize;
        List<TransactionLiveViewDTO> items = transactionRepository.findRecentTransactionsPage(pageSize, offset);

        TransactionLivePageResponse response = new TransactionLivePageResponse();
        response.setItems(items);
        response.setPage(effectivePage);
        response.setPageSize(pageSize);
        response.setTotalItems(totalItems);
        response.setTotalPages(totalPages);
        response.setHasPrevious(effectivePage > 1);
        response.setHasNext(effectivePage < totalPages);
        return response;
    }

    private int sanitizePage(Integer requestedPage) {
        if (requestedPage == null) {
            return DEFAULT_PAGE;
        }
        if (requestedPage < 1) {
            throw new IllegalArgumentException("page must be >= 1");
        }
        return requestedPage;
    }

    private int sanitizePageSize(Integer requestedPageSize) {
        if (requestedPageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (!ALLOWED_PAGE_SIZES.contains(requestedPageSize)) {
            throw new IllegalArgumentException("pageSize must be one of 25, 40, 60");
        }
        return requestedPageSize;
    }
}
