CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    version BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_price CHECK (price >= 0),
    CONSTRAINT chk_stock_quantity CHECK (stock_quantity >= 0)
);

CREATE INDEX idx_product_name ON products(name);
CREATE INDEX idx_product_price ON products(price);

