CREATE TABLE metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    protocol VARCHAR(50) NOT NULL,
    order_latency BIGINT,
    payment_latency BIGINT,
    shipping_latency BIGINT,
    total_latency BIGINT,
    compensations INT,
    order_status VARCHAR(50),
    payment_status VARCHAR(50),
    shipping_status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);