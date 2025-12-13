INSERT INTO shipments (order_id, customer_name, customer_email, shipping_address, carrier, tracking_number, status, estimated_delivery_date, actual_delivery_date, version, created_at, updated_at) VALUES
(1, 'John Doe', 'john.doe@example.com', '123 Main St, New York, NY 10001', 'FedEx', 'TRK-A1B2C3D4', 'DELIVERED', DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP), 0, DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(2, 'Jane Smith', 'jane.smith@example.com', '456 Oak Ave, Los Angeles, CA 90001', 'UPS', 'TRK-E5F6G7H8', 'IN_TRANSIT', DATEADD('DAY', 2, CURRENT_TIMESTAMP), NULL, 0, DATEADD('DAY', -2, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
(3, 'Bob Johnson', 'bob.johnson@example.com', '789 Pine Rd, Chicago, IL 60601', 'DHL', 'TRK-I9J0K1L2', 'PREPARING', DATEADD('DAY', 3, CURRENT_TIMESTAMP), NULL, 0, DATEADD('DAY', -1, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
(4, 'Alice Williams', 'alice.williams@example.com', '321 Elm St, Houston, TX 77001', 'USPS', 'TRK-M3N4O5P6', 'OUT_FOR_DELIVERY', CURRENT_TIMESTAMP, NULL, 0, DATEADD('DAY', -3, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
(5, 'Charlie Brown', 'charlie.brown@example.com', '654 Maple Dr, Phoenix, AZ 85001', 'FedEx', 'TRK-Q7R8S9T0', 'PENDING', DATEADD('DAY', 5, CURRENT_TIMESTAMP), NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

