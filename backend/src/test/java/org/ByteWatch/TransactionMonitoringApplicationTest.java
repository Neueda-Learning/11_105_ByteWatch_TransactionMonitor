package org.ByteWatch;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class TransactionMonitoringApplicationTest {

    @Test
    void constructor_createsInstance() {
        TransactionMonitoringApplication app = new TransactionMonitoringApplication();
        assertNotNull(app);
    }

    @Test
    void main_invokesSpringApplicationRun() {
        try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {
            springApplicationMock.when(() -> SpringApplication.run(eq(TransactionMonitoringApplication.class), any(String[].class)))
                    .thenReturn(mock(org.springframework.context.ConfigurableApplicationContext.class));

            TransactionMonitoringApplication.main(new String[]{});

            springApplicationMock.verify(() -> SpringApplication.run(eq(TransactionMonitoringApplication.class), any(String[].class)));
        }
    }
}
