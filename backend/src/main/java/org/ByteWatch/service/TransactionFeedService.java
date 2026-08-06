package org.ByteWatch.service;

import org.springframework.stereotype.Service;
import org.ByteWatch.model.TransactionLiveViewDTO;
import org.ByteWatch.repository.TransactionRepository;

import java.util.List;

/**
 * Read-only service for realtime transaction feed consumption.
 */
@Service
public class TransactionFeedService {

    private static final int DEFAULT_LIMIT = 40;
    private static final int MAX_LIMIT = 200;

    private final TransactionRepository transactionRepository;

    public TransactionFeedService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionLiveViewDTO> getRecentTransactions(Integer requestedLimit) {
        int limit = sanitizeLimit(requestedLimit);
        return transactionRepository.findRecentTransactions(limit);
    }

    private int sanitizeLimit(Integer requestedLimit) {
        if (requestedLimit == null) {
            return DEFAULT_LIMIT;
        }
        if (requestedLimit < 1 || requestedLimit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
        return requestedLimit;
    }
}
