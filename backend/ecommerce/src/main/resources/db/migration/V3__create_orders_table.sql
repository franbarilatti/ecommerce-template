-- ============================================
-- FILE: src/main/resources/db/migration/V3__create_orders_table.sql
-- Propósito: Crear tablas de órdenes, items y envío
-- Versión: 3
-- ============================================

-- Tabla de órdenes
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subtotal DECIMAL(10, 2) NOT NULL,
    shipping_cost DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total DECIMAL(10, 2) NOT NULL,
    customer_notes TEXT,
    admin_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,

    CONSTRAINT fk_order_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT check_order_status CHECK (status IN (
        'PENDING', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED'
    )),
    CONSTRAINT check_subtotal_positive CHECK (subtotal >= 0),
    CONSTRAINT check_shipping_cost_non_negative CHECK (shipping_cost >= 0),
    CONSTRAINT check_discount_non_negative CHECK (discount >= 0),
    CONSTRAINT check_total_positive CHECK (total >= 0)
);

-- Índices para órdenes
CREATE INDEX idx_order_user_id ON orders(user_id);
CREATE INDEX idx_order_number ON orders(order_number);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created_at ON orders(created_at);
CREATE INDEX idx_order_paid_at ON orders(paid_at);

-- Índices compuestos
CREATE INDEX idx_order_user_status ON orders(user_id, status);
CREATE INDEX idx_order_user_created ON orders(user_id, created_at DESC);

-- Comentarios
COMMENT ON TABLE orders IS 'Órdenes de compra';
COMMENT ON COLUMN orders.order_number IS 'Número único de orden visible al cliente';
COMMENT ON COLUMN orders.status IS 'Estado actual de la orden';
COMMENT ON COLUMN orders.customer_notes IS 'Notas del cliente sobre la orden';
COMMENT ON COLUMN orders.admin_notes IS 'Notas internas del administrador';

-- ============================================

-- Tabla de items de orden
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_price DECIMAL(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    line_total DECIMAL(10, 2) NOT NULL,
    product_image_url VARCHAR(500),

    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT check_quantity_positive CHECK (quantity > 0),
    CONSTRAINT check_product_price_positive CHECK (product_price > 0),
    CONSTRAINT check_line_total_positive CHECK (line_total >= 0)
);

-- Índices para order_items
CREATE INDEX idx_order_item_order_id ON order_items(order_id);
CREATE INDEX idx_order_item_product_id ON order_items(product_id);

-- Comentarios
COMMENT ON TABLE order_items IS 'Items individuales de cada orden';
COMMENT ON COLUMN order_items.product_name IS 'Nombre del producto al momento de la compra';
COMMENT ON COLUMN order_items.product_price IS 'Precio del producto al momento de la compra';
COMMENT ON COLUMN order_items.line_total IS 'Total de la línea (quantity * product_price)';

-- ============================================

-- Tabla de información de envío
CREATE TABLE shipping_info (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    street VARCHAR(100) NOT NULL,
    number VARCHAR(10) NOT NULL,
    floor VARCHAR(10),
    apartment VARCHAR(10),
    city VARCHAR(100) NOT NULL,
    province VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    reference TEXT,
    tracking_number VARCHAR(100),
    carrier VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_shipping_info_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE
);

-- Índices para shipping_info
CREATE INDEX idx_shipping_info_order_id ON shipping_info(order_id);
CREATE INDEX idx_shipping_info_tracking_number ON shipping_info(tracking_number);
CREATE INDEX idx_shipping_info_province ON shipping_info(province);
CREATE INDEX idx_shipping_info_city ON shipping_info(city);
CREATE INDEX idx_shipping_info_carrier ON shipping_info(carrier);

-- Comentarios
COMMENT ON TABLE shipping_info IS 'Información de envío de órdenes';
COMMENT ON COLUMN shipping_info.tracking_number IS 'Número de seguimiento del envío';
COMMENT ON COLUMN shipping_info.carrier IS 'Empresa transportista (OCA, Correo Argentino, etc.)';

-- ============================================

-- Triggers para actualizar updated_at
CREATE TRIGGER update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_shipping_info_updated_at
    BEFORE UPDATE ON shipping_info
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();