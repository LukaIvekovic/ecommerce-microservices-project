INSERT INTO payments (order_id, paid_customer_name, paid_customer_email, paid_amount, payment_method, status, transaction_id, payment_provider, card_last_four_digits, processed_at, version, created_at, updated_at) VALUES
(1, 'John Doe', 'john.doe@example.com', 2399.98, 'CREDIT_CARD', 'COMPLETED', 'TXN-a1b2c3d4-e5f6-7890-ab12-cd34ef567890', 'Stripe', '4242', CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Jane Smith', 'jane.smith@example.com', 1099.99, 'PAYPAL', 'COMPLETED', 'TXN-b2c3d4e5-f6a7-8901-bc23-de45fa678901', 'PayPal', NULL, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Bob Johnson', 'bob.johnson@example.com', 649.98, 'DEBIT_CARD', 'PROCESSING', 'TXN-c3d4e5f6-a7b8-9012-cd34-ef56ab789012', 'Stripe', '1234', NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Alice Williams', 'alice.williams@example.com', 399.99, 'CREDIT_CARD', 'COMPLETED', 'TXN-d4e5f6a7-b8c9-0123-de45-fa67bc890123', 'Square', '5678', CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Charlie Brown', 'charlie.brown@example.com', 1299.99, 'BANK_TRANSFER', 'PENDING', 'TXN-e5f6a7b8-c9d0-1234-ef56-ab78cd901234', 'Bank Transfer', NULL, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

