-- ============================================
-- FILE: src/main/resources/db/migration/V2__create_products_table.sql
-- Propósito: Crear tablas de productos, categorías e imágenes
-- Versión: 2
-- ============================================

-- Tabla de categorías
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para categorías
CREATE INDEX idx_category_slug ON categories(slug);
CREATE INDEX idx_category_active ON categories(active);
CREATE INDEX idx_category_display_order ON categories(display_order);

-- Comentarios
COMMENT ON TABLE categories IS 'Categorías de productos';
COMMENT ON COLUMN categories.slug IS 'URL amigable para SEO';
COMMENT ON COLUMN categories.display_order IS 'Orden de visualización en frontend';

-- ============================================

-- Tabla de productos
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    sale_price DECIMAL(10, 2),
    category_id BIGINT NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    weight DECIMAL(5, 2),
    is_new BOOLEAN NOT NULL DEFAULT FALSE,
    on_sale BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sku VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_category FOREIGN KEY (category_id)
        REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT check_price_positive CHECK (price > 0),
    CONSTRAINT check_sale_price_positive CHECK (sale_price IS NULL OR sale_price > 0),
    CONSTRAINT check_stock_non_negative CHECK (stock >= 0),
    CONSTRAINT check_weight_positive CHECK (weight IS NULL OR weight > 0)
);

-- Índices para productos
CREATE INDEX idx_product_category_id ON products(category_id);
CREATE INDEX idx_product_name ON products(name);
CREATE INDEX idx_product_price ON products(price);
CREATE INDEX idx_product_active ON products(active);
CREATE INDEX idx_product_is_new ON products(is_new);
CREATE INDEX idx_product_on_sale ON products(on_sale);
CREATE INDEX idx_product_stock ON products(stock);
CREATE INDEX idx_product_created_at ON products(created_at);
CREATE INDEX idx_product_sku ON products(sku);

-- Índice compuesto para búsquedas frecuentes
CREATE INDEX idx_product_category_active ON products(category_id, active);
CREATE INDEX idx_product_active_stock ON products(active, stock);

-- Comentarios
COMMENT ON TABLE products IS 'Productos del catálogo';
COMMENT ON COLUMN products.price IS 'Precio normal del producto';
COMMENT ON COLUMN products.sale_price IS 'Precio en oferta (si aplica)';
COMMENT ON COLUMN products.weight IS 'Peso en kilogramos para cálculo de envío';
COMMENT ON COLUMN products.is_new IS 'Producto marcado como nuevo';
COMMENT ON COLUMN products.on_sale IS 'Producto en oferta';
COMMENT ON COLUMN products.sku IS 'Código único del producto (Stock Keeping Unit)';

-- ============================================

-- Tabla de imágenes de productos
CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    public_id VARCHAR(200),
    alt_text VARCHAR(200),
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_image_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE
);

-- Índices para imágenes
CREATE INDEX idx_product_image_product_id ON product_images(product_id);
CREATE INDEX idx_product_image_is_main ON product_images(product_id, is_main);
CREATE INDEX idx_product_image_display_order ON product_images(product_id, display_order);

-- Comentarios
COMMENT ON TABLE product_images IS 'Imágenes de productos';
COMMENT ON COLUMN product_images.public_id IS 'ID público de Cloudinary para eliminar';
COMMENT ON COLUMN product_images.is_main IS 'Imagen principal del producto';
COMMENT ON COLUMN product_images.display_order IS 'Orden de visualización';

-- ============================================

-- Triggers para actualizar updated_at
CREATE TRIGGER update_categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();