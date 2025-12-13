CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    paid_customer_name VARCHAR(255) NOT NULL,
    paid_customer_email VARCHAR(255) NOT NULL,
    paid_amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255) UNIQUE,
    payment_provider VARCHAR(255),
    card_last_four_digits VARCHAR(4),
    failure_reason VARCHAR(500),
    processed_at TIMESTAMP,
    version BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_amount CHECK (paid_amount >= 0)
);

CREATE INDEX idx_payment_order_id ON payments(order_id);
CREATE INDEX idx_payment_paid_customer_email ON payments(paid_customer_email);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payment_created_at ON payments(created_at);

