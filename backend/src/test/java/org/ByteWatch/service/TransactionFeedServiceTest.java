package org.ByteWatch.service;

import org.ByteWatch.model.TransactionLivePageResponse;
import org.ByteWatch.model.TransactionLiveViewDTO;
import org.ByteWatch.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionFeedServiceTest {

    private TransactionRepository transactionRepository;
    private TransactionFeedService transactionFeedService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        transactionFeedService = new TransactionFeedService(transactionRepository);
    }

    @Test
    void getRecentTransactionsPage_usesDefaultsWhenParamsMissing() {
        when(transactionRepository.countTransactions()).thenReturn(0L);
        when(transactionRepository.findRecentTransactionsPage(eq(40), eq(0))).thenReturn(List.of());

        TransactionLivePageResponse response = transactionFeedService.getRecentTransactionsPage(null, null);

        assertEquals(1, response.getPage());
        assertEquals(40, response.getPageSize());
        assertEquals(0L, response.getTotalItems());
        assertEquals(1, response.getTotalPages());
        assertFalse(response.isHasPrevious());
        assertFalse(response.isHasNext());
    }

    @Test
    void getRecentTransactionsPage_capsRequestedPageToLastPage() {
        TransactionLiveViewDTO item = new TransactionLiveViewDTO();
        item.setTxnId("TXN-100");

        when(transactionRepository.countTransactions()).thenReturn(100L);
        when(transactionRepository.findRecentTransactionsPage(eq(25), eq(75))).thenReturn(List.of(item));

        TransactionLivePageResponse response = transactionFeedService.getRecentTransactionsPage(99, 25);

        assertEquals(4, response.getPage());
        assertEquals(25, response.getPageSize());
        assertEquals(100L, response.getTotalItems());
        assertEquals(4, response.getTotalPages());
        assertTrue(response.isHasPrevious());
        assertFalse(response.isHasNext());
        assertEquals("TXN-100", response.getItems().get(0).getTxnId());
    }

    @Test
    void getRecentTransactionsPage_rejectsInvalidPage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionFeedService.getRecentTransactionsPage(0, 25));

        assertEquals("page must be >= 1", ex.getMessage());
    }

    @Test
    void getRecentTransactionsPage_rejectsInvalidPageSize() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionFeedService.getRecentTransactionsPage(1, 15));

        assertEquals("pageSize must be one of 25, 40, 60", ex.getMessage());
    }
}
