package org.ByteWatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application bootstrap for the transaction monitoring backend.
 */
@SpringBootApplication
public class TransactionMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionMonitoringApplication.class, args);
    }
}