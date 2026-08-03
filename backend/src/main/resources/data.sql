-- Stage 0 reset: rebuild demo data on every startup so Swagger always shows the same state.
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM alert_log;
DELETE FROM alerts;
DELETE FROM transactions;
DELETE FROM customers;
ALTER TABLE alert_log AUTO_INCREMENT = 1;
ALTER TABLE alerts AUTO_INCREMENT = 1;
ALTER TABLE customers AUTO_INCREMENT = 1;
SET FOREIGN_KEY_CHECKS = 1;

-- Stage 1 seed: customer/account profile data used across dashboard lookups.
INSERT INTO customers (name, acc_num, acc_type, bank_name, currency) VALUES
('Ava Patel', 'ACC-1001', 'SAVINGS', 'Global Bank', 'USD'),
('Liam Carter', 'ACC-1002', 'CHECKING', 'Metro Credit', 'USD'),
('Sofia Nguyen', 'ACC-1003', 'SAVINGS', 'Harbor Bank', 'USD'),
('Noah Bennett', 'ACC-1004', 'CHECKING', 'Northern Trust', 'GBP'),
('Emma Wilson', 'ACC-1005', 'SAVINGS', 'Northern Trust', 'GBP'),
('Arjun Mehta', 'ACC-1006', 'CURRENT', 'Meridian Bank', 'INR'),
('Priya Shah', 'ACC-1007', 'SAVINGS', 'Meridian Bank', 'INR'),
('Lucas Weber', 'ACC-1008', 'CHECKING', 'Rhine Bank', 'EUR'),
('Mia Fischer', 'ACC-1009', 'SAVINGS', 'Rhine Bank', 'EUR'),
('Daniel Brooks', 'ACC-1010', 'BUSINESS', 'Apex Business Bank', 'USD'),
('Chloe Martin', 'ACC-1011', 'BUSINESS', 'Continental Savings', 'EUR'),
('Ethan Ross', 'ACC-1012', 'CHECKING', 'Cityline Bank', 'USD'),
('Grace Kim', 'ACC-1013', 'SAVINGS', 'Pacific First', 'USD'),
('Oliver Scott', 'ACC-1014', 'CURRENT', 'Sterling Bank', 'GBP'),
('Nina Verma', 'ACC-1015', 'SAVINGS', 'Unity Bank', 'INR');

-- Stage 2 seed: baseline transaction history generated for a realistic dashboard volume.
INSERT INTO transactions (txn_id, timestamp, amount, currency, payee_acc_num, payer_acc_num, status, type)
WITH RECURSIVE seq AS (
	SELECT 1 AS n
	UNION ALL
	SELECT n + 1 FROM seq WHERE n < 996
)
SELECT
	CONCAT('TXN-', LPAD(n, 4, '0')) AS txn_id,
	DATE_ADD('2026-06-01 08:00:00', INTERVAL n * 11 MINUTE) AS timestamp,
	CAST(ROUND(85 + MOD(n * 37, 4200) + ((MOD(n, 5)) * 0.47), 2) AS DECIMAL(15, 2)) AS amount,
	CASE MOD(n, 4)
		WHEN 0 THEN 'USD'
		WHEN 1 THEN 'GBP'
		WHEN 2 THEN 'EUR'
		ELSE 'INR'
	END AS currency,
	CASE MOD(n + 5, 15)
		WHEN 0 THEN 'ACC-1001'
		WHEN 1 THEN 'ACC-1002'
		WHEN 2 THEN 'ACC-1003'
		WHEN 3 THEN 'ACC-1004'
		WHEN 4 THEN 'ACC-1005'
		WHEN 5 THEN 'ACC-1006'
		WHEN 6 THEN 'ACC-1007'
		WHEN 7 THEN 'ACC-1008'
		WHEN 8 THEN 'ACC-1009'
		WHEN 9 THEN 'ACC-1010'
		WHEN 10 THEN 'ACC-1011'
		WHEN 11 THEN 'ACC-1012'
		WHEN 12 THEN 'ACC-1013'
		WHEN 13 THEN 'ACC-1014'
		ELSE 'ACC-1015'
	END AS payee_acc_num,
	CASE MOD(n, 15)
		WHEN 0 THEN 'ACC-1001'
		WHEN 1 THEN 'ACC-1002'
		WHEN 2 THEN 'ACC-1003'
		WHEN 3 THEN 'ACC-1004'
		WHEN 4 THEN 'ACC-1005'
		WHEN 5 THEN 'ACC-1006'
		WHEN 6 THEN 'ACC-1007'
		WHEN 7 THEN 'ACC-1008'
		WHEN 8 THEN 'ACC-1009'
		WHEN 9 THEN 'ACC-1010'
		WHEN 10 THEN 'ACC-1011'
		WHEN 11 THEN 'ACC-1012'
		WHEN 12 THEN 'ACC-1013'
		WHEN 13 THEN 'ACC-1014'
		ELSE 'ACC-1015'
	END AS payer_acc_num,
	CASE
		WHEN MOD(n, 23) = 0 THEN 'PENDING'
		WHEN MOD(n, 41) = 0 THEN 'FAILED'
		ELSE 'COMPLETED'
	END AS status,
	CASE MOD(n, 4)
		WHEN 0 THEN 'TRANSFER'
		WHEN 1 THEN 'PAYMENT'
		WHEN 2 THEN 'WIRE'
		ELSE 'TRANSFER'
	END AS type
FROM seq;

-- Stage 2 seed extension: hand-crafted suspicious transactions that line up with the demo alerts.
INSERT INTO transactions (txn_id, timestamp, amount, currency, payee_acc_num, payer_acc_num, status, type) VALUES
('TXN-1001', '2026-07-14 09:05:00', 22000.00, 'USD', 'ACC-1003', 'ACC-1010', 'PENDING', 'TRANSFER'),
('TXN-1002', '2026-07-14 10:25:00', 27500.00, 'USD', 'ACC-1012', 'ACC-1010', 'PENDING', 'WIRE'),
('TXN-1003', '2026-07-14 11:15:00', 18350.00, 'USD', 'ACC-1002', 'ACC-1010', 'PENDING', 'TRANSFER'),
('TXN-1004', '2026-07-15 08:40:00', 9200.00, 'EUR', 'ACC-1008', 'ACC-1011', 'COMPLETED', 'PAYMENT'),
('TXN-1005', '2026-07-16 12:00:00', 6800.00, 'GBP', 'ACC-1014', 'ACC-1005', 'COMPLETED', 'PAYMENT'),
('TXN-1006', '2026-07-16 12:01:00', 7100.00, 'GBP', 'ACC-1004', 'ACC-1005', 'COMPLETED', 'TRANSFER'),
('TXN-1007', '2026-07-16 12:02:00', 6950.00, 'GBP', 'ACC-1012', 'ACC-1005', 'COMPLETED', 'TRANSFER'),
('TXN-1008', '2026-07-16 12:03:00', 7350.00, 'GBP', 'ACC-1013', 'ACC-1005', 'COMPLETED', 'TRANSFER'),
('TXN-1009', '2026-07-18 16:45:00', 14800.00, 'INR', 'ACC-1001', 'ACC-1007', 'PENDING', 'WIRE'),
('TXN-1010', '2026-07-18 16:46:20', 15100.00, 'INR', 'ACC-1006', 'ACC-1007', 'PENDING', 'WIRE'),
('TXN-1011', '2026-07-18 16:47:40', 14200.00, 'INR', 'ACC-1015', 'ACC-1007', 'PENDING', 'TRANSFER'),
('TXN-1012', '2026-07-20 19:10:00', 54000.00, 'USD', 'ACC-1009', 'ACC-1010', 'PENDING', 'WIRE');

-- Stage 3 seed: alerts inserted directly because seeded transactions do not flow through the live rule engine.
INSERT INTO alerts (id, status, severity_score, txn_id, alert_timestamp, rule_ids) VALUES
(1, 'OPEN', 40, 'TXN-1001', '2026-07-14 09:06:10', '1'),
(2, 'ACKNOWLEDGED', 65, 'TXN-1002', '2026-07-14 10:26:30', '1,4'),
(3, 'INVESTIGATING', 65, 'TXN-1003', '2026-07-14 11:16:05', '1,4'),
(4, 'DISMISSED', 15, 'TXN-1004', '2026-07-15 08:41:15', '3'),
(5, 'OPEN', 45, 'TXN-1008', '2026-07-16 12:03:45', '2,3'),
(6, 'ACKNOWLEDGED', 80, 'TXN-1010', '2026-07-18 16:47:10', '1,2,3'),
(7, 'INVESTIGATING', 85, 'TXN-1011', '2026-07-18 16:48:00', '1,2,3'),
(8, 'CLOSED', 65, 'TXN-1012', '2026-07-20 19:11:00', '1,4');

-- Stage 4 seed: audit trail for dismissed/closed/in-progress demo scenarios.
INSERT INTO alert_log (alert_id, comment, status, log_timestamp) VALUES
(1, 'Auto-generated from overnight monitoring batch.', 'OPEN', '2026-07-14 09:06:10'),
(2, 'Risk analyst acknowledged after high-value transfer review.', 'ACKNOWLEDGED', '2026-07-14 10:31:00'),
(3, 'Escalated to investigation queue pending customer callback.', 'INVESTIGATING', '2026-07-14 11:24:00'),
(4, 'Reviewed travel pattern and confirmed legitimate vendor payment.', 'DISMISSED', '2026-07-15 09:10:00'),
(5, 'Velocity spike opened automatically for rapid outbound transfers.', 'OPEN', '2026-07-16 12:03:45'),
(6, 'Analyst acknowledged clustered outbound activity from ACC-1007.', 'ACKNOWLEDGED', '2026-07-18 16:52:00'),
(7, 'Customer unreachable; investigation remains active.', 'INVESTIGATING', '2026-07-18 17:10:00'),
(8, 'Business account transfer verified with treasury operations.', 'CLOSED', '2026-07-20 20:05:00');

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