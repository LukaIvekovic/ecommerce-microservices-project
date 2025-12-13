INSERT INTO orders (customer_name, customer_email, shipping_address, total_amount, status, version, created_at, updated_at) VALUES
('John Doe', 'john.doe@example.com', '123 Main St, New York, NY 10001', 2399.98, 'DELIVERED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Jane Smith', 'jane.smith@example.com', '456 Oak Ave, Los Angeles, CA 90001', 1099.99, 'SHIPPED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Bob Johnson', 'bob.johnson@example.com', '789 Pine Rd, Chicago, IL 60601', 649.98, 'PROCESSING', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Alice Williams', 'alice.williams@example.com', '321 Elm St, Houston, TX 77001', 399.99, 'CONFIRMED', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Charlie Brown', 'charlie.brown@example.com', '654 Maple Dr, Phoenix, AZ 85001', 1299.99, 'PENDING', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Order items for Order 1 (John Doe) - Laptop and iPhone
INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
(1, 1, 'Laptop Dell XPS 13', 1, 1299.99, 1299.99),
(1, 2, 'iPhone 14 Pro', 1, 1099.99, 1099.99);

-- Order items for Order 2 (Jane Smith) - iPhone only
INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
(2, 2, 'iPhone 14 Pro', 1, 1099.99, 1099.99);

-- Order items for Order 3 (Bob Johnson) - iPad and Wireless Charger
INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
(3, 5, 'iPad Air', 1, 599.99, 599.99),
(3, 13, 'Wireless Charger', 1, 29.99, 29.99),
(3, 14, 'Gaming Mouse Pad', 1, 34.99, 34.99);

-- Order items for Order 4 (Alice Williams) - Headphones
INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
(4, 4, 'Sony WH-1000XM5', 1, 399.99, 399.99);

-- Order items for Order 5 (Charlie Brown) - Laptop
INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, subtotal) VALUES
(5, 1, 'Laptop Dell XPS 13', 1, 1299.99, 1299.99);

