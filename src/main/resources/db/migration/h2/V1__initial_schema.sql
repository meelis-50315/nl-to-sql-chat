-- ============================================================
-- V1 - Initial e-commerce schema (H2)
-- ============================================================

CREATE TABLE customers (
    id          UUID                     DEFAULT RANDOM_UUID() PRIMARY KEY,
    email       VARCHAR                  NOT NULL UNIQUE,
    first_name  VARCHAR                  NOT NULL,
    last_name   VARCHAR                  NOT NULL,
    phone       VARCHAR,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
    id          UUID                     DEFAULT RANDOM_UUID() PRIMARY KEY,
    name        VARCHAR                  NOT NULL,
    description VARCHAR,
    price       NUMERIC(10, 2)           NOT NULL CHECK (price >= 0),
    stock       INTEGER                  NOT NULL DEFAULT 0 CHECK (stock >= 0),
    category    VARCHAR                  NOT NULL,
    active      BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE orders (
    id              UUID                     DEFAULT RANDOM_UUID() PRIMARY KEY,
    customer_id     UUID                     NOT NULL REFERENCES customers(id),
    status          VARCHAR                  NOT NULL DEFAULT 'PENDING'
                                              CHECK (status IN ('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED')),
    total_amount    NUMERIC(10, 2)           NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    shipping_address VARCHAR                 NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id          UUID                     DEFAULT RANDOM_UUID() PRIMARY KEY,
    order_id    UUID                     NOT NULL REFERENCES orders(id),
    product_id  UUID                     NOT NULL REFERENCES products(id),
    quantity    INTEGER                  NOT NULL CHECK (quantity > 0),
    unit_price  NUMERIC(10, 2)           NOT NULL CHECK (unit_price >= 0),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_orders_customer_id   ON orders(customer_id);
CREATE INDEX idx_orders_status        ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_products_category    ON products(category);
CREATE INDEX idx_products_active      ON products(active);
