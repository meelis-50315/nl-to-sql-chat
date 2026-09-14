-- ============================================================
-- V1 - Initial e-commerce schema
-- ============================================================

CREATE TABLE customers (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email       TEXT        NOT NULL UNIQUE,
    first_name  TEXT        NOT NULL,
    last_name   TEXT        NOT NULL,
    phone       TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
    id          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT           NOT NULL,
    description TEXT,
    price       NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    stock       INTEGER        NOT NULL DEFAULT 0 CHECK (stock >= 0),
    category    TEXT           NOT NULL,
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE orders (
    id              UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     UUID           NOT NULL REFERENCES customers(id),
    status          TEXT           NOT NULL DEFAULT 'PENDING'
                                   CHECK (status IN ('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED')),
    total_amount    NUMERIC(10, 2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    shipping_address TEXT          NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id    UUID           NOT NULL REFERENCES orders(id),
    product_id  UUID           NOT NULL REFERENCES products(id),
    quantity    INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price  NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0),
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_orders_customer_id   ON orders(customer_id);
CREATE INDEX idx_orders_status        ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_products_category    ON products(category);
CREATE INDEX idx_products_active      ON products(active);
