// FILE: scripts/confirmation.js
// Propósito: Lógica de la página de confirmación de pedido
// Descripción: Mostrar detalles del pedido confirmado desde localStorage
// Uso: Importar en confirmation.html después de main.js

(function() {
    'use strict';

    // ===== ELEMENTOS DEL DOM =====
    const elements = {
        orderId: document.getElementById('orderId'),
        orderDate: document.getElementById('orderDate'),
        orderProducts: document.getElementById('orderProducts'),
        orderSubtotal: document.getElementById('orderSubtotal'),
        orderShipping: document.getElementById('orderShipping'),
        orderTotal: document.getElementById('orderTotal'),
        customerInfo: document.getElementById('customerInfo'),
        shippingInfo: document.getElementById('shippingInfo'),
        paymentInfo: document.getElementById('paymentInfo'),
        statusDate1: document.getElementById('statusDate1'),
        printOrderBtn: document.getElementById('printOrderBtn')
    };

    // ===== UTILIDADES =====

    /**
     * Formatear precio
     */
    function formatPrice(price) {
        return `$${price.toLocaleString('es-AR')}`;
    }

    /**
     * Formatear fecha
     */
    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('es-AR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    }

    /**
     * Formatear hora
     */
    function formatTime(dateString) {
        const date = new Date(dateString);
        return date.toLocaleTimeString('es-AR', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    /**
     * Obtener ícono según categoría
     */
    function getCategoryIcon(category) {
        const icons = {
            'baby': '👶',
            'boy': '👔',
            'girl': '👗',
            'party': '🎉',
            'accessories': '🎀'
        };
        return icons[category] || '👕';
    }

    /**
     * Obtener nombre del método de pago
     */
    function getPaymentMethodName(method) {
        const names = {
            'transfer': 'Transferencia Bancaria',
            'mercadopago': 'MercadoPago',
            'cash': 'Efectivo'
        };
        return names[method] || method;
    }

    // ===== CARGAR PEDIDO =====

    /**
     * Obtener último pedido del localStorage
     */
    function getLastOrder() {
        const orders = JSON.parse(localStorage.getItem('aguardi_orders') || '[]');
        
        if (orders.length === 0) {
            return null;
        }
        
        // Ordenar por fecha y obtener el más reciente
        orders.sort((a, b) => new Date(b.date) - new Date(a.date));
        return orders[0];
    }

    /**
     * Renderizar información del pedido
     */
    function renderOrder(order) {
        if (!order) {
            // Redirigir si no hay pedido
            window.location.href = 'index.html';
            return;
        }

        // Order ID
        elements.orderId.textContent = `#${order.id.split('-')[1] || order.id}`;

        // Order Date
        const date = new Date(order.date);
        elements.orderDate.textContent = formatDate(order.date);
        elements.statusDate1.textContent = `${formatDate(order.date)} - ${formatTime(order.date)}`;

        // Products
        renderProducts(order.items);

        // Totals
        elements.orderSubtotal.textContent = formatPrice(order.totals.subtotal);
        
        if (order.totals.shipping === 0) {
            elements.orderShipping.textContent = '¡Gratis!';
            elements.orderShipping.style.color = 'var(--success)';
            elements.orderShipping.style.fontWeight = '700';
        } else {
            elements.orderShipping.textContent = formatPrice(order.totals.shipping);
        }
        
        elements.orderTotal.textContent = formatPrice(order.totals.total);

        // Customer Info
        renderCustomerInfo(order.customer);

        // Shipping Info
        renderShippingInfo(order.shipping);

        // Payment Info
        renderPaymentInfo(order.payment);
    }

    /**
     * Renderizar productos
     */
    function renderProducts(items) {
        elements.orderProducts.innerHTML = '';

        items.forEach(item => {
            const productDiv = document.createElement('div');
            productDiv.className = 'order-product-item';

            productDiv.innerHTML = `
                <div class="product-image-small">
                    <span>${getCategoryIcon(item.category)}</span>
                </div>
                <div class="product-details">
                    <div class="product-name">${item.name}</div>
                    <div class="product-quantity">Cantidad: ${item.quantity}</div>
                </div>
                <div class="product-price">${formatPrice(item.price * item.quantity)}</div>
            `;

            elements.orderProducts.appendChild(productDiv);
        });
    }

    /**
     * Renderizar información del cliente
     */
    function renderCustomerInfo(customer) {
        elements.customerInfo.innerHTML = `
            <div><strong>Nombre:</strong> ${customer.fullName}</div>
            <div><strong>Email:</strong> ${customer.email}</div>
            <div><strong>Teléfono:</strong> ${customer.phone}</div>
        `;
    }

    /**
     * Renderizar información de envío
     */
    function renderShippingInfo(shipping) {
        elements.shippingInfo.innerHTML = `
            <div><strong>Dirección:</strong> ${shipping.address}</div>
            ${shipping.details ? `<div>${shipping.details}</div>` : ''}
            <div><strong>Localidad:</strong> ${shipping.city}</div>
            <div><strong>Provincia:</strong> ${shipping.province}</div>
            <div><strong>CP:</strong> ${shipping.postalCode}</div>
        `;
    }

    /**
     * Renderizar información de pago
     */
    function renderPaymentInfo(payment) {
        const paymentMethodName = getPaymentMethodName(payment.method);
        const statusText = payment.status === 'pending' ? 'Pendiente' : 'Completado';
        const statusClass = payment.status === 'pending' ? 'status-pending' : 'status-completed';

        elements.paymentInfo.innerHTML = `
            <div><strong>Método:</strong> ${paymentMethodName}</div>
            <div><strong>Estado:</strong> <span class="status-badge ${statusClass}">${statusText}</span></div>
        `;

        // Información adicional según el método de pago
        if (payment.method === 'transfer') {
            elements.paymentInfo.innerHTML += `
                <div style="margin-top: var(--space-4); padding: var(--space-3); background-color: var(--gray-50); border-radius: var(--radius-md);">
                    <div style="font-size: var(--font-size-sm); color: var(--gray-700);">
                        <strong>Nota:</strong> Te enviaremos los datos para la transferencia por WhatsApp
                    </div>
                </div>
            `;
        } else if (payment.method === 'cash') {
            elements.paymentInfo.innerHTML += `
                <div style="margin-top: var(--space-4); padding: var(--space-3); background-color: var(--gray-50); border-radius: var(--radius-md);">
                    <div style="font-size: var(--font-size-sm); color: var(--gray-700);">
                        <strong>Nota:</strong> Pago contra entrega
                    </div>
                </div>
            `;
        }
    }

    // ===== ACCIONES =====

    /**
     * Imprimir pedido
     */
    function printOrder() {
        window.print();
    }

    // ===== EVENT LISTENERS =====

    /**
     * Inicializar event listeners
     */
    function initEventListeners() {
        // Botón de imprimir
        if (elements.printOrderBtn) {
            elements.printOrderBtn.addEventListener('click', printOrder);
        }
    }

    // ===== INICIALIZACIÓN =====

    /**
     * Inicializar página de confirmación
     */
    function init() {
        const order = getLastOrder();
        renderOrder(order);
        initEventListeners();

        console.log('Página de confirmación AGUARDI inicializada');
    }

    // Inicializar cuando el DOM esté listo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();