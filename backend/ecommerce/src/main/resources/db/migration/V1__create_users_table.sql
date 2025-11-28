-- ============================================
-- FILE: src/main/resources/db/migration/V1__create_users_table.sql
-- Propósito: Crear tabla de usuarios y direcciones
-- Versión: 1
-- ============================================

-- Tabla de usuarios
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'CLIENT',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,

    CONSTRAINT check_role CHECK (role IN ('CLIENT', 'ADMIN'))
);

-- Índices para usuarios
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_created_at ON users(created_at);
CREATE INDEX idx_user_enabled ON users(enabled);

-- Comentarios
COMMENT ON TABLE users IS 'Tabla de usuarios del sistema';
COMMENT ON COLUMN users.role IS 'Rol del usuario: CLIENT o ADMIN';
COMMENT ON COLUMN users.enabled IS 'Usuario activo/inactivo';
COMMENT ON COLUMN users.email_verified IS 'Email verificado';

-- ============================================

-- Tabla de direcciones
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    street VARCHAR(100) NOT NULL,
    number VARCHAR(10) NOT NULL,
    floor VARCHAR(10),
    apartment VARCHAR(10),
    city VARCHAR(100) NOT NULL,
    province VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    reference VARCHAR(200),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_address_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

-- Índices para direcciones
CREATE INDEX idx_address_user_id ON addresses(user_id);
CREATE INDEX idx_address_province ON addresses(province);
CREATE INDEX idx_address_city ON addresses(city);
CREATE INDEX idx_address_postal_code ON addresses(postal_code);
CREATE INDEX idx_address_is_default ON addresses(user_id, is_default);

-- Comentarios
COMMENT ON TABLE addresses IS 'Direcciones de envío de usuarios';
COMMENT ON COLUMN addresses.is_default IS 'Dirección predeterminada del usuario';
COMMENT ON COLUMN addresses.postal_code IS 'Código postal argentino (4 dígitos)';

-- ============================================

-- Función para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger para users
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger para addresses
CREATE TRIGGER update_addresses_updated_at
    BEFORE UPDATE ON addresses
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();