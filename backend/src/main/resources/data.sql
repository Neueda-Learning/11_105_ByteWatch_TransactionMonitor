-- Stage 1 seed: customer/account profile data.
-- Use INSERT IGNORE so local restarts don't fail on duplicate keys.
INSERT IGNORE INTO customers (name, acc_num, acc_type, bank_name, currency) VALUES
('Alice Smith', 'ACC-1001', 'SAVINGS', 'Global Bank', 'USD'),
('Bob Jones', 'ACC-1002', 'CHECKING', 'UK Trust', 'GBP'),
('Priya Patel', 'ACC-1003', 'SAVINGS', 'Mumbai Finance', 'INR'),
('Hans Muller', 'ACC-1004', 'BUSINESS', 'Euro Vault', 'EUR'),
('Charlie Brown', 'ACC-1005', 'CHECKING', 'Global Bank', 'USD');

-- Stage 2 seed: baseline + suspicious transactions for rule validation.
INSERT IGNORE INTO transactions (txn_id, timestamp, amount, currency, payee_acc_num, payer_acc_num, status, type) VALUES
-- Normal baseline transactions (No alerts expected)
('TXN-001', '2023-10-25 10:00:00', 150.00, 'USD', 'ACC-1002', 'ACC-1001', 'COMPLETED', 'TRANSFER'),
('TXN-002', '2023-10-25 10:15:00', 4500.00, 'GBP', 'ACC-1004', 'ACC-1002', 'COMPLETED', 'PAYMENT'),

-- High Amount Rule Triggers (>$10,000)
('TXN-003', '2023-10-25 10:30:00', 15000.00, 'USD', 'ACC-1005', 'ACC-1001', 'PENDING', 'TRANSFER'),
('TXN-004', '2023-10-25 11:00:00', 850000.00, 'INR', 'ACC-1001', 'ACC-1003', 'PENDING', 'WIRE'),

-- Velocity Rule Triggers (Multiple rapid transactions from ACC-1005 in a 2-minute window)
('TXN-005', '2023-10-25 11:05:00', 200.00, 'USD', 'ACC-1002', 'ACC-1005', 'COMPLETED', 'TRANSFER'),
('TXN-006', '2023-10-25 11:05:15', 250.00, 'USD', 'ACC-1003', 'ACC-1005', 'COMPLETED', 'TRANSFER'),
('TXN-007', '2023-10-25 11:05:45', 300.00, 'USD', 'ACC-1004', 'ACC-1005', 'COMPLETED', 'TRANSFER'),
('TXN-008', '2023-10-25 11:06:10', 150.00, 'USD', 'ACC-1001', 'ACC-1005', 'COMPLETED', 'TRANSFER'),

-- Cross-Currency / New Payee Test
('TXN-009', '2023-10-25 11:30:00', 1200.00, 'EUR', 'ACC-9999', 'ACC-1004', 'COMPLETED', 'PAYMENT');