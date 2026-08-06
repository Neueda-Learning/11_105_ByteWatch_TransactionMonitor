package org.ByteWatch;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionMonitoringApplicationTest {

    @Test
    void constructor_createsInstance() {
        TransactionMonitoringApplication app = new TransactionMonitoringApplication();
        assertNotNull(app);
    }
}
