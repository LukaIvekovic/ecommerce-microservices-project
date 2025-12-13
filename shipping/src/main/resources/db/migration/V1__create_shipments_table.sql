CREATE TABLE shipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(500) NOT NULL,
    carrier VARCHAR(100) NOT NULL,
    tracking_number VARCHAR(100) UNIQUE,
    status VARCHAR(50) NOT NULL,
    estimated_delivery_date TIMESTAMP,
    actual_delivery_date TIMESTAMP,
    version BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_shipment_order_id ON shipments(order_id);
CREATE INDEX idx_shipment_tracking_number ON shipments(tracking_number);
CREATE INDEX idx_shipment_customer_email ON shipments(customer_email);
CREATE INDEX idx_shipment_status ON shipments(status);
CREATE INDEX idx_shipment_created_at ON shipments(created_at);

