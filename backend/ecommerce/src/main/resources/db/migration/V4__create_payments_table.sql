-- ============================================
-- FILE: src/main/resources/db/migration/V4__create_payments_table.sql
-- Propósito: Crear tablas de pagos y logs de webhooks
-- Versión: 4
-- ============================================

-- Tabla de pagos
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    method VARCHAR(20) NOT NULL,
    external_payment_id VARCHAR(100) UNIQUE,
    preference_id VARCHAR(100),
    merchant_order_id VARCHAR(100),
    payment_details TEXT,
    status_detail VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    refunded_at TIMESTAMP,

    CONSTRAINT fk_payment_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE RESTRICT,
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT check_payment_status CHECK (status IN (
        'PENDING', 'IN_PROCESS', 'APPROVED', 'REJECTED', 'CANCELLED', 'REFUNDED', 'CHARGED_BACK'
    )),
    CONSTRAINT check_payment_method CHECK (method IN (
        'CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'MERCADOPAGO', 'CASH', 'OTHER'
    )),
    CONSTRAINT check_amount_positive CHECK (amount > 0)
);

-- Índices para payments
CREATE INDEX idx_payment_order_id ON payments(order_id);
CREATE INDEX idx_payment_user_id ON payments(user_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_method ON payments(method);
CREATE INDEX idx_payment_external_id ON payments(external_payment_id);
CREATE INDEX idx_payment_created_at ON payments(created_at);
CREATE INDEX idx_payment_approved_at ON payments(approved_at);

-- Índices compuestos
CREATE INDEX idx_payment_user_status ON payments(user_id, status);
CREATE INDEX idx_payment_status_approved ON payments(status, approved_at);

-- Comentarios
COMMENT ON TABLE payments IS 'Pagos de órdenes';
COMMENT ON COLUMN payments.external_payment_id IS 'ID del pago en MercadoPago';
COMMENT ON COLUMN payments.preference_id IS 'ID de preferencia de MercadoPago';
COMMENT ON COLUMN payments.merchant_order_id IS 'ID de orden del comerciante en MercadoPago';
COMMENT ON COLUMN payments.payment_details IS 'JSON con detalles adicionales del pago';
COMMENT ON COLUMN payments.status_detail IS 'Detalle del estado (ej: rejected by insufficient_amount)';

-- ============================================

-- Tabla de logs de webhooks
CREATE TABLE payment_webhook_logs (
    id BIGSERIAL PRIMARY KEY,
    external_payment_id VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

-- Índices para webhook_logs
CREATE INDEX idx_webhook_external_payment_id ON payment_webhook_logs(external_payment_id);
CREATE INDEX idx_webhook_action ON payment_webhook_logs(action);
CREATE INDEX idx_webhook_processed ON payment_webhook_logs(processed);
CREATE INDEX idx_webhook_created_at ON payment_webhook_logs(created_at);

-- Comentarios
COMMENT ON TABLE payment_webhook_logs IS 'Logs de webhooks de MercadoPago para debugging';
COMMENT ON COLUMN payment_webhook_logs.action IS 'Tipo de acción (payment.created, payment.updated, etc.)';
COMMENT ON COLUMN payment_webhook_logs.payload IS 'JSON completo del webhook';
COMMENT ON COLUMN payment_webhook_logs.processed IS 'Indica si el webhook fue procesado';

-- ============================================

-- Triggers para actualizar updated_at
CREATE TRIGGER update_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================

-- Vista para estadísticas de pagos
CREATE OR REPLACE VIEW payment_statistics AS
SELECT
    COUNT(*) as total_payments,
    COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) as approved_payments,
    COUNT(CASE WHEN status = 'REJECTED' THEN 1 END) as rejected_payments,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_payments,
    COALESCE(SUM(CASE WHEN status = 'APPROVED' THEN amount ELSE 0 END), 0) as total_revenue,
    COALESCE(AVG(CASE WHEN status = 'APPROVED' THEN amount END), 0) as average_payment,
    COUNT(DISTINCT user_id) as unique_payers,
    DATE_TRUNC('day', created_at) as payment_date
FROM payments
GROUP BY DATE_TRUNC('day', created_at)
ORDER BY payment_date DESC;

COMMENT ON VIEW payment_statistics IS 'Vista con estadísticas diarias de pagos';