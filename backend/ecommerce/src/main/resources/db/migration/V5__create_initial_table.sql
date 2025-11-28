-- ============================================
-- FILE: src/main/resources/db/migration/V5__insert_initial_data.sql
-- Propósito: Insertar datos iniciales (admin, categorías, productos de ejemplo)
-- Versión: 5
-- ============================================

-- ============================================
-- USUARIOS INICIALES
-- ============================================

-- Usuario Administrador
-- Email: admin@aguardi.com
-- Password: Admin123! (debe ser hasheado en la aplicación)
-- NOTA: Este password es temporal y debe cambiarse en el primer login
INSERT INTO users (first_name, last_name, email, password, phone, role, enabled, email_verified)
VALUES (
    'Admin',
    'AGUARDI',
    'admin@aguardi.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8vyhrfn4gk/xfBl1sqFxuWtmSN6lu', -- Password: Admin123!
    '+5492236000000',
    'ADMIN',
    TRUE,
    TRUE
);

-- Usuario Cliente de Prueba
-- Email: cliente@test.com
-- Password: Cliente123!
INSERT INTO users (first_name, last_name, email, password, phone, role, enabled, email_verified)
VALUES (
    'María',
    'González',
    'cliente@test.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- Password: Cliente123!
    '+5492236111111',
    'CLIENT',
    TRUE,
    TRUE
);

-- Dirección de ejemplo para el cliente
INSERT INTO addresses (user_id, street, number, floor, apartment, city, province, postal_code, is_default)
SELECT
    id,
    'Av. Independencia',
    '1234',
    '5',
    'B',
    'Mar del Plata',
    'Buenos Aires',
    '7600',
    TRUE
FROM users WHERE email = 'cliente@test.com';

-- ============================================
-- CATEGORÍAS
-- ============================================

INSERT INTO categories (name, slug, description, display_order, active) VALUES
('Bebé', 'bebe', 'Ropa y accesorios para bebés de 0 a 24 meses', 1, TRUE),
('Niño', 'nino', 'Ropa y accesorios para niños', 2, TRUE),
('Niña', 'nina', 'Ropa y accesorios para niñas', 3, TRUE),
('Fiesta', 'fiesta', 'Ropa de fiesta, trajes y vestidos elegantes', 4, TRUE),
('Accesorios', 'accesorios', 'Corbatas, tiradores, moños y más complementos', 5, TRUE);

-- ============================================
-- PRODUCTOS DE EJEMPLO
-- ============================================

-- Productos categoría BEBÉ
INSERT INTO products (name, description, price, category_id, stock, is_new, active, sku)
SELECT
    'Enterito de Algodón para Bebé',
    'Enterito suave de algodón 100% para bebé. Disponible en varios colores. Ideal para el día a día.',
    8500.00,
    id,
    25,
    TRUE,
    TRUE,
    'BEB-ENT-001'
FROM categories WHERE slug = 'bebe';

INSERT INTO products (name, description, price, category_id, stock, is_new, active, sku)
SELECT
    'Conjunto 3 Piezas Bebé',
    'Conjunto de 3 piezas: body, pantalón y gorro. Material suave y cómodo para la piel del bebé.',
    12500.00,
    id,
    15,
    TRUE,
    TRUE,
    'BEB-CON-002'
FROM categories WHERE slug = 'bebe';

-- Productos categoría NIÑO
INSERT INTO products (name, description, price, sale_price, category_id, stock, on_sale, active, sku)
SELECT
    'Camisa de Vestir para Niño',
    'Camisa elegante de vestir para niños. Perfecta para eventos formales. Tallas 4 a 12 años.',
    15000.00,
    12000.00,
    id,
    30,
    TRUE,
    TRUE,
    'NIN-CAM-001'
FROM categories WHERE slug = 'nino';

INSERT INTO products (name, description, price, category_id, stock, active, sku)
SELECT
    'Pantalón de Vestir para Niño',
    'Pantalón elegante para niños. Ajustable y cómodo. Ideal para combinar con camisa.',
    13500.00,
    id,
    20,
    TRUE,
    'NIN-PAN-002'
FROM categories WHERE slug = 'nino';

-- Productos categoría NIÑA
INSERT INTO products (name, description, price, category_id, stock, is_new, active, sku)
SELECT
    'Vestido de Algodón Floreado',
    'Hermoso vestido de algodón con estampado floral. Perfecto para primavera y verano.',
    16500.00,
    id,
    18,
    TRUE,
    TRUE,
    'NIN-VES-001'
FROM categories WHERE slug = 'nina';

INSERT INTO products (name, description, price, sale_price, category_id, stock, on_sale, active, sku)
SELECT
    'Conjunto Falda y Remera',
    'Conjunto de falda y remera a juego. Diseño moderno y cómodo para uso diario.',
    14000.00,
    11200.00,
    id,
    22,
    TRUE,
    TRUE,
    'NIN-CON-002'
FROM categories WHERE slug = 'nina';

-- Productos categoría FIESTA
INSERT INTO products (name, description, price, category_id, stock, is_new, active, sku)
SELECT
    'Traje Completo para Niño',
    'Traje elegante completo: saco, pantalón, camisa y corbata. Perfecto para eventos especiales.',
    45000.00,
    id,
    8,
    TRUE,
    TRUE,
    'FIE-TRA-001'
FROM categories WHERE slug = 'fiesta';

INSERT INTO products (name, description, price, category_id, stock, is_new, active, sku)
SELECT
    'Vestido de Fiesta con Tul',
    'Hermoso vestido de fiesta con falda de tul. Ideal para cumpleaños y celebraciones.',
    38000.00,
    id,
    10,
    TRUE,
    TRUE,
    'FIE-VES-002'
FROM categories WHERE slug = 'fiesta';

-- Productos categoría ACCESORIOS
INSERT INTO products (name, description, price, category_id, stock, active, sku)
SELECT
    'Corbata Elegante para Niño',
    'Corbata de seda para niños. Varios colores disponibles. Combina perfecto con cualquier traje.',
    5500.00,
    id,
    50,
    TRUE,
    'ACC-COR-001'
FROM categories WHERE slug = 'accesorios';

INSERT INTO products (name, description, price, sale_price, category_id, stock, on_sale, active, sku)
SELECT
    'Tiradores Ajustables',
    'Tiradores elásticos ajustables para niños. Diseño clásico y elegante.',
    4500.00,
    3600.00,
    id,
    40,
    TRUE,
    TRUE,
    'ACC-TIR-002'
FROM categories WHERE slug = 'accesorios';

INSERT INTO products (name, description, price, category_id, stock, active, sku)
SELECT
    'Moño para Cabello',
    'Moño decorativo para cabello. Varios colores y diseños. Perfecto para ocasiones especiales.',
    3500.00,
    id,
    60,
    TRUE,
    'ACC-MON-003'
FROM categories WHERE slug = 'accesorios';

-- ============================================
-- COMENTARIOS FINALES
-- ============================================

-- Actualizar secuencias (por si acaso)
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));
SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));
SELECT setval('addresses_id_seq', (SELECT MAX(id) FROM addresses));

-- ============================================
-- RESUMEN DE DATOS INICIALES
-- ============================================

-- Verificar que todo se insertó correctamente
DO $$
DECLARE
    user_count INT;
    category_count INT;
    product_count INT;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO category_count FROM categories;
    SELECT COUNT(*) INTO product_count FROM products;

    RAISE NOTICE '====================================';
    RAISE NOTICE 'DATOS INICIALES INSERTADOS';
    RAISE NOTICE '====================================';
    RAISE NOTICE 'Usuarios creados: %', user_count;
    RAISE NOTICE 'Categorías creadas: %', category_count;
    RAISE NOTICE 'Productos creados: %', product_count;
    RAISE NOTICE '====================================';
    RAISE NOTICE 'Usuario Admin: admin@aguardi.com';
    RAISE NOTICE 'Password temporal: Admin123!';
    RAISE NOTICE '====================================';
    RAISE NOTICE 'IMPORTANTE: Cambiar el password del';
    RAISE NOTICE 'usuario admin en el primer login';
    RAISE NOTICE '====================================';
END $$;