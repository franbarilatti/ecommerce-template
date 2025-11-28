-- ============================================
-- FILE: src/main/resources/db/migration/V6__create_views_and_functions.sql
-- Propósito: Crear vistas y funciones útiles para estadísticas y reportes
-- Versión: 6
-- ============================================

-- ============================================
-- VISTAS PARA ESTADÍSTICAS
-- ============================================

-- Vista de productos con información completa
CREATE OR REPLACE VIEW v_products_full AS
SELECT
    p.id,
    p.name,
    p.description,
    p.price,
    p.sale_price,
    p.stock,
    p.is_new,
    p.on_sale,
    p.active,
    p.sku,
    c.id as category_id,
    c.name as category_name,
    c.slug as category_slug,
    CASE
        WHEN p.on_sale AND p.sale_price IS NOT NULL THEN p.sale_price
        ELSE p.price
    END as effective_price,
    CASE
        WHEN p.on_sale AND p.sale_price IS NOT NULL THEN
            ROUND(((p.price - p.sale_price) / p.price * 100)::numeric, 0)
        ELSE 0
    END as discount_percentage,
    p.stock > 0 as in_stock,
    p.created_at,
    p.updated_at
FROM products p
INNER JOIN categories c ON p.category_id = c.id;

COMMENT ON VIEW v_products_full IS 'Vista completa de productos con información calculada';

-- ============================================

-- Vista de órdenes con información del usuario
CREATE OR REPLACE VIEW v_orders_full AS
SELECT
    o.id,
    o.order_number,
    o.status,
    o.subtotal,
    o.shipping_cost,
    o.discount,
    o.total,
    u.id as user_id,
    u.email as user_email,
    CONCAT(u.first_name, ' ', u.last_name) as user_full_name,
    o.created_at,
    o.paid_at,
    o.shipped_at,
    o.delivered_at,
    o.cancelled_at,
    COUNT(oi.id) as item_count,
    si.city as shipping_city,
    si.province as shipping_province,
    p.status as payment_status
FROM orders o
INNER JOIN users u ON o.user_id = u.id
LEFT JOIN order_items oi ON o.id = oi.order_id
LEFT JOIN shipping_info si ON o.id = si.order_id
LEFT JOIN payments p ON o.id = p.order_id
GROUP BY o.id, u.id, u.email, u.first_name, u.last_name,
         si.city, si.province, p.status;

COMMENT ON VIEW v_orders_full IS 'Vista completa de órdenes con información del usuario';

-- ============================================

-- Vista de ventas diarias
CREATE OR REPLACE VIEW v_daily_sales AS
SELECT
    DATE(created_at) as sale_date,
    COUNT(*) as total_orders,
    COUNT(CASE WHEN status IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED') THEN 1 END) as completed_orders,
    COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_orders,
    COALESCE(SUM(CASE WHEN status IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED') THEN total ELSE 0 END), 0) as total_revenue,
    COALESCE(AVG(CASE WHEN status IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED') THEN total END), 0) as average_order_value
FROM orders
GROUP BY DATE(created_at)
ORDER BY sale_date DESC;

COMMENT ON VIEW v_daily_sales IS 'Vista de ventas agregadas por día';

-- ============================================

-- Vista de productos más vendidos
CREATE OR REPLACE VIEW v_top_selling_products AS
SELECT
    p.id,
    p.name,
    p.price,
    c.name as category_name,
    COUNT(oi.id) as times_ordered,
    SUM(oi.quantity) as total_quantity_sold,
    SUM(oi.line_total) as total_revenue
FROM products p
LEFT JOIN order_items oi ON p.id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.id
LEFT JOIN categories c ON p.category_id = c.id
WHERE o.status IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED')
   OR o.status IS NULL
GROUP BY p.id, p.name, p.price, c.name
ORDER BY total_quantity_sold DESC NULLS LAST;

COMMENT ON VIEW v_top_selling_products IS 'Vista de productos más vendidos';

-- ============================================

-- Vista de stock bajo
CREATE OR REPLACE VIEW v_low_stock_products AS
SELECT
    p.id,
    p.name,
    p.sku,
    p.stock,
    c.name as category_name,
    p.price,
    p.active
FROM products p
INNER JOIN categories c ON p.category_id = c.id
WHERE p.stock > 0 AND p.stock <= 5 AND p.active = TRUE
ORDER BY p.stock ASC;

COMMENT ON VIEW v_low_stock_products IS 'Vista de productos con stock bajo (5 o menos)';

-- ============================================
-- FUNCIONES ÚTILES
-- ============================================

-- Función para calcular ingresos de un período
CREATE OR REPLACE FUNCTION calculate_revenue(
    start_date TIMESTAMP,
    end_date TIMESTAMP
)
RETURNS DECIMAL(10, 2) AS $$
DECLARE
    total_revenue DECIMAL(10, 2);
BEGIN
    SELECT COALESCE(SUM(total), 0)
    INTO total_revenue
    FROM orders
    WHERE paid_at BETWEEN start_date AND end_date
      AND status IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED');

    RETURN total_revenue;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION calculate_revenue IS 'Calcular ingresos totales entre dos fechas';

-- ============================================

-- Función para obtener top N clientes
CREATE OR REPLACE FUNCTION get_top_customers(
    limit_count INTEGER DEFAULT 10
)
RETURNS TABLE (
    user_id BIGINT,
    user_email VARCHAR(100),
    user_name VARCHAR(101),
    total_orders BIGINT,
    total_spent DECIMAL(10, 2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        u.id,
        u.email,
        CONCAT(u.first_name, ' ', u.last_name),
        COUNT(o.id)::BIGINT,
        COALESCE(SUM(o.total), 0)::DECIMAL(10, 2)
    FROM users u
    INNER JOIN orders o ON u.id = o.user_id
    WHERE o.status IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED')
    GROUP BY u.id, u.email, u.first_name, u.last_name
    ORDER BY SUM(o.total) DESC
    LIMIT limit_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_top_customers IS 'Obtener los N mejores clientes por monto gastado';

-- ============================================

-- Función para verificar disponibilidad de stock
CREATE OR REPLACE FUNCTION check_product_stock(
    product_id_param BIGINT,
    quantity_param INTEGER
)
RETURNS BOOLEAN AS $$
DECLARE
    current_stock INTEGER;
BEGIN
    SELECT stock INTO current_stock
    FROM products
    WHERE id = product_id_param AND active = TRUE;

    IF current_stock IS NULL THEN
        RETURN FALSE;
    END IF;

    RETURN current_stock >= quantity_param;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION check_product_stock IS 'Verificar si hay stock disponible para un producto';

-- ============================================

-- Función para reservar stock (transaccional)
CREATE OR REPLACE FUNCTION reserve_product_stock(
    product_id_param BIGINT,
    quantity_param INTEGER
)
RETURNS BOOLEAN AS $$
DECLARE
    current_stock INTEGER;
BEGIN
    -- Lock la fila para evitar race conditions
    SELECT stock INTO current_stock
    FROM products
    WHERE id = product_id_param AND active = TRUE
    FOR UPDATE;

    IF current_stock IS NULL OR current_stock < quantity_param THEN
        RETURN FALSE;
    END IF;

    -- Reducir stock
    UPDATE products
    SET stock = stock - quantity_param
    WHERE id = product_id_param;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION reserve_product_stock IS 'Reservar stock de producto de forma segura';

-- ============================================

-- Función para restaurar stock (en caso de cancelación)
CREATE OR REPLACE FUNCTION restore_product_stock(
    product_id_param BIGINT,
    quantity_param INTEGER
)
RETURNS VOID AS $$
BEGIN
    UPDATE products
    SET stock = stock + quantity_param
    WHERE id = product_id_param;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION restore_product_stock IS 'Restaurar stock de producto';

-- ============================================

-- Función para obtener estadísticas del dashboard
CREATE OR REPLACE FUNCTION get_dashboard_stats()
RETURNS JSON AS $$
DECLARE
    result JSON;
    today_start TIMESTAMP := DATE_TRUNC('day', CURRENT_TIMESTAMP);
    month_start TIMESTAMP := DATE_TRUNC('month', CURRENT_TIMESTAMP);
BEGIN
    SELECT json_build_object(
        'total_users', (SELECT COUNT(*) FROM users WHERE enabled = TRUE),
        'total_products', (SELECT COUNT(*) FROM products WHERE active = TRUE),
        'total_orders', (SELECT COUNT(*) FROM orders),
        'products_low_stock', (SELECT COUNT(*) FROM v_low_stock_products),
        'today_orders', (SELECT COUNT(*) FROM orders WHERE created_at >= today_start),
        'today_revenue', (SELECT COALESCE(SUM(total), 0) FROM orders WHERE paid_at >= today_start AND status IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED')),
        'month_orders', (SELECT COUNT(*) FROM orders WHERE created_at >= month_start),
        'month_revenue', (SELECT COALESCE(SUM(total), 0) FROM orders WHERE paid_at >= month_start AND status IN ('PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED')),
        'pending_orders', (SELECT COUNT(*) FROM orders WHERE status = 'PENDING'),
        'processing_orders', (SELECT COUNT(*) FROM orders WHERE status IN ('PAID', 'PROCESSING'))
    ) INTO result;

    RETURN result;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION get_dashboard_stats IS 'Obtener estadísticas completas para el dashboard admin';

-- ============================================

-- Función para limpiar logs antiguos de webhooks (mantenimiento)
CREATE OR REPLACE FUNCTION cleanup_old_webhook_logs(
    days_old INTEGER DEFAULT 30
)
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM payment_webhook_logs
    WHERE created_at < CURRENT_TIMESTAMP - (days_old || ' days')::INTERVAL
      AND processed = TRUE;

    GET DIAGNOSTICS deleted_count = ROW_COUNT;

    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION cleanup_old_webhook_logs IS 'Limpiar logs de webhooks antiguos procesados';