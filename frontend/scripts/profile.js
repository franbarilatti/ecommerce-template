// FILE: scripts/profile.js
// Propósito: Lógica de la página de perfil de usuario
// Descripción: Ver/editar datos, historial de pedidos, cambiar contraseña
// Uso: Importar en profile.html después de main.js

// ============================================
// FILE: frontend/scripts/profile.js (ACTUALIZADO)
// Propósito: Perfil de usuario y gestión de órdenes conectado con el backend
// ============================================

// ============================================
// FILE: frontend/scripts/confirmation.js (ACTUALIZADO)
// Propósito: Página de confirmación de orden conectada con el backend
// ============================================

let orderData = null;

// ========================================
// CARGAR ORDEN
// ========================================

async function loadOrderConfirmation() {
    try {
        // Obtener parámetros de la URL
        const urlParams = new URLSearchParams(window.location.search);
        const orderNumber = urlParams.get('orderNumber');
        const orderId = urlParams.get('orderId');
        const paymentStatus = urlParams.get('status'); // De MercadoPago
        const paymentId = urlParams.get('payment_id'); // De MercadoPago

        // Si viene de MercadoPago con payment_id, verificar estado del pago
        if (paymentId) {
            await checkPaymentStatus(paymentId);
        }

        // Cargar orden
        if (orderNumber) {
            orderData = await API.orders.getOrderByNumber(orderNumber);
        } else if (orderId) {
            orderData = await API.orders.getOrderById(orderId);
        } else {
            throw new Error('No se encontró información de la orden');
        }

        // Renderizar confirmación
        renderConfirmation(paymentStatus);

        // Mostrar confetti si el pago fue exitoso
        if (paymentStatus === 'approved' || orderData.status === 'PAID') {
            showConfetti();
        }

    } catch (error) {
        console.error('Error cargando confirmación:', error);
        showErrorState();
    }
}

// ========================================
// VERIFICAR ESTADO DEL PAGO
// ========================================

async function checkPaymentStatus(paymentId) {
    try {
        const paymentStatus = await API.payments.checkPaymentStatus(paymentId);
        console.log('Payment status:', paymentStatus);
        
        // El backend ya habrá actualizado la orden vía webhook
        // Aquí solo mostramos el estado actual
        
    } catch (error) {
        console.error('Error verificando pago:', error);
    }
}

// ========================================
// RENDERIZAR CONFIRMACIÓN
// ========================================

function renderConfirmation(paymentStatus) {
    const order = orderData;

    // Icono y mensaje según estado
    let statusIcon, statusMessage, statusClass;

    if (paymentStatus === 'approved' || order.status === 'PAID') {
        statusIcon = 'fas fa-check-circle';
        statusMessage = '¡Compra Exitosa!';
        statusClass = 'success';
    } else if (paymentStatus === 'pending' || order.status === 'PENDING') {
        statusIcon = 'fas fa-clock';
        statusMessage = 'Pago Pendiente';
        statusClass = 'pending';
    } else if (paymentStatus === 'rejected') {
        statusIcon = 'fas fa-times-circle';
        statusMessage = 'Pago Rechazado';
        statusClass = 'error';
    } else {
        statusIcon = 'fas fa-info-circle';
        statusMessage = 'Orden Recibida';
        statusClass = 'info';
    }

    // Header
    const headerEl = document.getElementById('confirmationHeader');
    if (headerEl) {
        headerEl.innerHTML = `
            <div class="confirmation-icon ${statusClass}">
                <i class="${statusIcon}"></i>
            </div>
            <h1>${statusMessage}</h1>
            <p class="order-number">Orden #${order.orderNumber}</p>
        `;
    }

    // Mensaje
    const messageEl = document.getElementById('confirmationMessage');
    if (messageEl) {
        let message = '';
        
        if (order.status === 'PAID') {
            message = `
                <p>¡Gracias por tu compra! Tu orden ha sido confirmada y está siendo procesada.</p>
                <p>Recibirás un email de confirmación en <strong>${order.user?.email || 'tu correo'}</strong> con los detalles de tu orden.</p>
                <p>Te notificaremos cuando tu pedido sea enviado.</p>
            `;
        } else if (order.status === 'PENDING') {
            message = `
                <p>Tu orden ha sido registrada pero el pago aún está pendiente.</p>
                <p>Una vez que se confirme el pago, procesaremos tu pedido.</p>
            `;
        } else {
            message = `
                <p>Tu orden ha sido registrada en nuestro sistema.</p>
            `;
        }

        messageEl.innerHTML = message;
    }

    // Detalles de la orden
    renderOrderDetails();

    // Información de envío
    renderShippingInfo();

    // Resumen de pago
    renderPaymentSummary();
}

// ========================================
// DETALLES DE LA ORDEN
// ========================================

function renderOrderDetails() {
    const order = orderData;
    const detailsEl = document.getElementById('orderDetails');

    if (!detailsEl) return;

    detailsEl.innerHTML = `
        <h3>Productos</h3>
        <div class="order-items-list">
            ${order.items.map(item => `
                <div class="order-item-row">
                    <img src="${item.productImageUrl || '/images/placeholder.jpg'}" 
                         alt="${item.productName}">
                    <div class="item-info">
                        <strong>${item.productName}</strong>
                        <p>Cantidad: ${item.quantity}</p>
                    </div>
                    <div class="item-price">
                        $${item.lineTotal.toFixed(2)}
                    </div>
                </div>
            `).join('')}
        </div>
    `;
}

// ========================================
// INFORMACIÓN DE ENVÍO
// ========================================

function renderShippingInfo() {
    const order = orderData;
    const shippingEl = document.getElementById('shippingInfo');

    if (!shippingEl) return;

    const shipping = order.shippingInfo;

    shippingEl.innerHTML = `
        <h3>Información de Envío</h3>
        <div class="shipping-details">
            <p><strong>Destinatario:</strong> ${shipping.recipientName}</p>
            <p><strong>Teléfono:</strong> ${shipping.recipientPhone}</p>
            <p>
                <strong>Dirección:</strong><br>
                ${shipping.street} ${shipping.number}
                ${shipping.floor ? `, Piso ${shipping.floor}` : ''}
                ${shipping.apartment ? `, Depto ${shipping.apartment}` : ''}<br>
                ${shipping.city}, ${shipping.province}<br>
                CP: ${shipping.postalCode}, ${shipping.country}
            </p>
            ${shipping.trackingNumber ? `
                <div class="tracking-info">
                    <p><strong>Número de seguimiento:</strong> ${shipping.trackingNumber}</p>
                    <p><strong>Transportista:</strong> ${shipping.carrier || 'Por asignar'}</p>
                </div>
            ` : `
                <p class="text-muted">
                    <i class="fas fa-truck"></i> 
                    El número de seguimiento se te enviará cuando se despache el pedido.
                </p>
            `}
        </div>
    `;
}

// ========================================
// RESUMEN DE PAGO
// ========================================

function renderPaymentSummary() {
    const order = orderData;
    const summaryEl = document.getElementById('paymentSummary');

    if (!summaryEl) return;

    summaryEl.innerHTML = `
        <h3>Resumen</h3>
        <div class="payment-breakdown">
            <div class="summary-row">
                <span>Subtotal:</span>
                <span>$${order.subtotal.toFixed(2)}</span>
            </div>
            <div class="summary-row">
                <span>Envío:</span>
                <span>${order.shippingCost === 0 ? 'Gratis' : '$' + order.shippingCost.toFixed(2)}</span>
            </div>
            ${order.discount > 0 ? `
                <div class="summary-row discount">
                    <span>Descuento:</span>
                    <span>-$${order.discount.toFixed(2)}</span>
                </div>
            ` : ''}
            <div class="summary-row total">
                <strong>Total:</strong>
                <strong>$${order.total.toFixed(2)}</strong>
            </div>
        </div>

        <div class="payment-method">
            <p><strong>Método de pago:</strong> ${getPaymentMethodName(order)}</p>
            <p><strong>Estado:</strong> 
                <span class="status-badge status-${order.status.toLowerCase()}">
                    ${translateOrderStatus(order.status)}
                </span>
            </p>
        </div>
    `;
}

// ========================================
// ACCIONES
// ========================================

function viewMyOrders() {
    window.location.href = '/profile.html#orders';
}

function continueShopping() {
    window.location.href = '/catalog.html';
}

async function downloadInvoice() {
    // TODO: Implementar descarga de factura desde el backend
    showNotification('Funcionalidad en desarrollo', 'info');
}

function shareOrder() {
    // Compartir en redes sociales o copiar link
    const orderUrl = window.location.href;
    
    if (navigator.share) {
        navigator.share({
            title: 'Mi compra en AGUARDI',
            text: `¡Acabo de realizar una compra! Orden #${orderData.orderNumber}`,
            url: orderUrl
        }).catch(err => console.log('Error sharing:', err));
    } else {
        // Copiar al portapapeles
        navigator.clipboard.writeText(orderUrl).then(() => {
            showNotification('Link copiado al portapapeles', 'success');
        });
    }
}

// ========================================
// ERROR STATE
// ========================================

function showErrorState() {
    const container = document.querySelector('.confirmation-container');
    if (!container) return;

    container.innerHTML = `
        <div class="error-state">
            <div class="confirmation-icon error">
                <i class="fas fa-exclamation-triangle"></i>
            </div>
            <h1>No se pudo cargar la orden</h1>
            <p>No pudimos encontrar la información de tu orden.</p>
            <p>Si realizaste una compra, revisa tu email o contacta al soporte.</p>
            <div class="error-actions">
                <a href="/profile.html" class="btn btn-primary">Ver Mis Órdenes</a>
                <a href="/catalog.html" class="btn btn-secondary">Volver al Catálogo</a>
            </div>
        </div>
    `;
}

// ========================================
// CONFETTI ANIMATION
// ========================================

function showConfetti() {
    // Crear confetti si el pago fue exitoso
    const duration = 3000;
    const animationEnd = Date.now() + duration;
    const defaults = { startVelocity: 30, spread: 360, ticks: 60, zIndex: 0 };

    function randomInRange(min, max) {
        return Math.random() * (max - min) + min;
    }

    const interval = setInterval(function() {
        const timeLeft = animationEnd - Date.now();

        if (timeLeft <= 0) {
            return clearInterval(interval);
        }

        const particleCount = 50 * (timeLeft / duration);
        
        // Crear confetti visual simple con divs
        for (let i = 0; i < particleCount; i++) {
            const confetti = document.createElement('div');
            confetti.style.cssText = `
                position: fixed;
                width: 10px;
                height: 10px;
                background: ${['#ff0000', '#00ff00', '#0000ff', '#ffff00'][Math.floor(Math.random() * 4)]};
                top: -20px;
                left: ${randomInRange(0, 100)}%;
                animation: fall ${randomInRange(2, 4)}s linear;
                z-index: 9999;
            `;
            document.body.appendChild(confetti);

            setTimeout(() => {
                confetti.remove();
            }, 4000);
        }
    }, 250);

    // Agregar estilos de animación
    if (!document.getElementById('confetti-styles')) {
        const style = document.createElement('style');
        style.id = 'confetti-styles';
        style.textContent = `
            @keyframes fall {
                to {
                    transform: translateY(100vh) rotate(360deg);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(style);
    }
}

// ========================================
// HELPERS
// ========================================

function getPaymentMethodName(order) {
    // Por ahora, asumimos MercadoPago
    return 'MercadoPago';
}

function translateOrderStatus(status) {
    const translations = {
        'PENDING': 'Pendiente',
        'PAID': 'Pagada',
        'PROCESSING': 'En Proceso',
        'SHIPPED': 'Enviada',
        'DELIVERED': 'Entregada',
        'CANCELLED': 'Cancelada',
        'REFUNDED': 'Reembolsada'
    };
    return translations[status] || status;
}

// ========================================
// INICIALIZACIÓN
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    // Cargar confirmación
    loadOrderConfirmation();

    // Event listeners
    const viewOrdersBtn = document.getElementById('viewOrdersBtn');
    const continueShoppingBtn = document.getElementById('continueShoppingBtn');
    const downloadInvoiceBtn = document.getElementById('downloadInvoiceBtn');
    const shareOrderBtn = document.getElementById('shareOrderBtn');

    if (viewOrdersBtn) {
        viewOrdersBtn.addEventListener('click', viewMyOrders);
    }

    if (continueShoppingBtn) {
        continueShoppingBtn.addEventListener('click', continueShopping);
    }

    if (downloadInvoiceBtn) {
        downloadInvoiceBtn.addEventListener('click', downloadInvoice);
    }

    if (shareOrderBtn) {
        shareOrderBtn.addEventListener('click', shareOrder);
    }
});

// Hacer funciones disponibles globalmente
window.viewMyOrders = viewMyOrders;
window.continueShopping = continueShopping;
window.downloadInvoice = downloadInvoice;
window.shareOrder = shareOrder;