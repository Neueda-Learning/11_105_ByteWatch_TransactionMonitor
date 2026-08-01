CREATE TABLE transactions (
    txn_id VARCHAR(255) PRIMARY KEY,
    timestamp TIMESTAMP,
    amount DECIMAL(15, 2),
    currency VARCHAR(3), -- Either GBP, INR, USD, EUR
    payee_acc_num VARCHAR(255),
    payer_acc_num VARCHAR(255),
    status VARCHAR(50),
    type VARCHAR(50)
);

CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    acc_num VARCHAR(255),
    acc_type VARCHAR(50),
    bank_name VARCHAR(255),
);

CREATE TABLE alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(50),
    severity_score INT,
    txn_id VARCHAR(255),
    alert_timestamp TIMESTAMP,
    rule_ids VARCHAR(255)
);

CREATE TABLE alert_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_id BIGINT,
    comment VARCHAR(255),
    status VARCHAR(50),
    log_timestamp TIMESTAMP
);