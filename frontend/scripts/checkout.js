// FILE: scripts/checkout.js
// Propósito: Lógica del checkout
// Descripción: Validación de formulario, cálculo de totales, procesamiento de pedido
// Uso: Importar en checkout.html después de main.js

(function() {
    'use strict';

    // ===== CONFIGURACIÓN =====
    const CONFIG = {
        SHIPPING_COST: 2000,
        SHIPPING_FREE_THRESHOLD: 50000,
        WHATSAPP_NUMBER: '5492231234567'
    };

    // ===== ESTADO =====
    let state = {
        cart: [],
        subtotal: 0,
        shipping: 0,
        total: 0,
        formData: {}
    };

    // ===== ELEMENTOS DEL DOM =====
    const elements = {
        // Form
        checkoutForm: document.getElementById('checkoutForm'),
        
        // Inputs
        fullName: document.getElementById('fullName'),
        email: document.getElementById('email'),
        phone: document.getElementById('phone'),
        province: document.getElementById('province'),
        city: document.getElementById('city'),
        address: document.getElementById('address'),
        postalCode: document.getElementById('postalCode'),
        addressDetails: document.getElementById('addressDetails'),
        notes: document.getElementById('notes'),
        terms: document.getElementById('terms'),
        
        // Payment method
        paymentMethods: document.querySelectorAll('input[name="paymentMethod"]'),
        
        // Summary
        summaryProducts: document.getElementById('summaryProducts'),
        summarySubtotal: document.getElementById('summarySubtotal'),
        summaryShipping: document.getElementById('summaryShipping'),
        summaryTotal: document.getElementById('summaryTotal')
    };

    // ===== UTILIDADES =====

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
     * Formatear precio
     */
    function formatPrice(price) {
        return `$${price.toLocaleString('es-AR')}`;
    }

    /**
     * Validar email
     */
    function isValidEmail(email) {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return regex.test(email);
    }

    /**
     * Validar teléfono argentino
     */
    function isValidPhone(phone) {
        const regex = /^(\+?54)?[\s\-]?9?[\s\-]?\d{2,4}[\s\-]?\d{6,8}$/;
        return regex.test(phone);
    }

    /**
     * Mostrar error en campo
     */
    function showFieldError(fieldId, message) {
        const field = document.getElementById(fieldId);
        const errorElement = document.getElementById(`${fieldId}Error`);
        
        if (field) {
            field.classList.add('error');
        }
        
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.classList.add('show');
        }
    }

    /**
     * Limpiar error de campo
     */
    function clearFieldError(fieldId) {
        const field = document.getElementById(fieldId);
        const errorElement = document.getElementById(`${fieldId}Error`);
        
        if (field) {
            field.classList.remove('error');
        }
        
        if (errorElement) {
            errorElement.textContent = '';
            errorElement.classList.remove('show');
        }
    }

    /**
     * Limpiar todos los errores
     */
    function clearAllErrors() {
        const errorElements = document.querySelectorAll('.form-error');
        const inputElements = document.querySelectorAll('.form-input, .form-select');
        
        errorElements.forEach(el => {
            el.textContent = '';
            el.classList.remove('show');
        });
        
        inputElements.forEach(el => {
            el.classList.remove('error');
        });
    }

    // ===== CARRITO =====

    /**
     * Cargar carrito desde localStorage
     */
    function loadCart() {
        if (window.AGUARDI && window.AGUARDI.getCart) {
            state.cart = window.AGUARDI.getCart();
        } else {
            state.cart = [];
        }
        
        // Si el carrito está vacío, redirigir
        if (state.cart.length === 0) {
            window.location.href = 'cart.html';
            return;
        }
        
        calculateTotals();
        renderSummary();
    }

    /**
     * Calcular totales
     */
    function calculateTotals() {
        // Subtotal
        state.subtotal = state.cart.reduce((sum, item) => {
            return sum + (item.price * item.quantity);
        }, 0);
        
        // Envío
        if (state.subtotal >= CONFIG.SHIPPING_FREE_THRESHOLD) {
            state.shipping = 0;
        } else {
            state.shipping = CONFIG.SHIPPING_COST;
        }
        
        // Total
        state.total = state.subtotal + state.shipping;
    }

    /**
     * Renderizar resumen del pedido
     */
    function renderSummary() {
        // Renderizar productos
        elements.summaryProducts.innerHTML = '';
        
        state.cart.forEach(item => {
            const productElement = createSummaryProductElement(item);
            elements.summaryProducts.appendChild(productElement);
        });
        
        // Renderizar totales
        elements.summarySubtotal.textContent = formatPrice(state.subtotal);
        
        if (state.shipping === 0) {
            elements.summaryShipping.textContent = '¡Gratis!';
            elements.summaryShipping.style.color = 'var(--success)';
            elements.summaryShipping.style.fontWeight = '700';
        } else {
            elements.summaryShipping.textContent = formatPrice(state.shipping);
            elements.summaryShipping.style.color = '';
            elements.summaryShipping.style.fontWeight = '';
        }
        
        elements.summaryTotal.textContent = formatPrice(state.total);
    }

    /**
     * Crear elemento de producto en resumen
     */
    function createSummaryProductElement(item) {
        const div = document.createElement('div');
        div.className = 'summary-product';
        
        const itemTotal = item.price * item.quantity;
        
        div.innerHTML = `
            <div class="summary-product-image">
                <div class="image-placeholder">
                    <span class="placeholder-icon">${getCategoryIcon(item.category)}</span>
                </div>
            </div>
            <div class="summary-product-info">
                <div class="summary-product-name">${item.name}</div>
                <div class="summary-product-details">Cantidad: ${item.quantity}</div>
            </div>
            <div class="summary-product-price">${formatPrice(itemTotal)}</div>
        `;
        
        return div;
    }

    // ===== VALIDACIÓN DEL FORMULARIO =====

    /**
     * Validar formulario completo
     */
    function validateForm() {
        clearAllErrors();
        let isValid = true;
        
        // Nombre completo
        if (!elements.fullName.value.trim()) {
            showFieldError('fullName', 'El nombre es obligatorio');
            isValid = false;
        } else if (elements.fullName.value.trim().length < 3) {
            showFieldError('fullName', 'El nombre debe tener al menos 3 caracteres');
            isValid = false;
        }
        
        // Email
        if (!elements.email.value.trim()) {
            showFieldError('email', 'El email es obligatorio');
            isValid = false;
        } else if (!isValidEmail(elements.email.value.trim())) {
            showFieldError('email', 'El email no es válido');
            isValid = false;
        }
        
        // Teléfono
        if (!elements.phone.value.trim()) {
            showFieldError('phone', 'El teléfono es obligatorio');
            isValid = false;
        } else if (!isValidPhone(elements.phone.value.trim())) {
            showFieldError('phone', 'El teléfono no es válido');
            isValid = false;
        }
        
        // Provincia
        if (!elements.province.value) {
            showFieldError('province', 'Selecciona una provincia');
            isValid = false;
        }
        
        // Ciudad
        if (!elements.city.value.trim()) {
            showFieldError('city', 'La localidad es obligatoria');
            isValid = false;
        }
        
        // Dirección
        if (!elements.address.value.trim()) {
            showFieldError('address', 'La dirección es obligatoria');
            isValid = false;
        }
        
        // Código postal
        if (!elements.postalCode.value.trim()) {
            showFieldError('postalCode', 'El código postal es obligatorio');
            isValid = false;
        }
        
        // Términos y condiciones
        if (!elements.terms.checked) {
            showFieldError('terms', 'Debes aceptar los términos y condiciones');
            isValid = false;
        }
        
        return isValid;
    }

    /**
     * Obtener método de pago seleccionado
     */
    function getSelectedPaymentMethod() {
        const selected = Array.from(elements.paymentMethods).find(radio => radio.checked);
        return selected ? selected.value : null;
    }

    /**
     * Obtener nombre del método de pago
     */
    function getPaymentMethodName(value) {
        const names = {
            'transfer': 'Transferencia Bancaria',
            'mercadopago': 'MercadoPago',
            'cash': 'Efectivo'
        };
        return names[value] || value;
    }

    // ===== PROCESAMIENTO DEL PEDIDO =====

    /**
     * Generar mensaje de WhatsApp
     */
    function generateWhatsAppMessage(orderData) {
        let message = `🛍️ *NUEVO PEDIDO - AGUARDI*\n\n`;
        
        // Información del cliente
        message += `*📋 DATOS DEL CLIENTE*\n`;
        message += `Nombre: ${orderData.fullName}\n`;
        message += `Email: ${orderData.email}\n`;
        message += `Teléfono: ${orderData.phone}\n\n`;
        
        // Dirección de envío
        message += `*📍 DIRECCIÓN DE ENVÍO*\n`;
        message += `Provincia: ${orderData.province}\n`;
        message += `Localidad: ${orderData.city}\n`;
        message += `Dirección: ${orderData.address}\n`;
        message += `Código Postal: ${orderData.postalCode}\n`;
        if (orderData.addressDetails) {
            message += `Detalles: ${orderData.addressDetails}\n`;
        }
        message += `\n`;
        
        // Productos
        message += `*🛒 PRODUCTOS*\n`;
        state.cart.forEach((item, index) => {
            message += `${index + 1}. ${item.name}\n`;
            message += `   Cantidad: ${item.quantity}\n`;
            message += `   Precio unitario: ${formatPrice(item.price)}\n`;
            message += `   Subtotal: ${formatPrice(item.price * item.quantity)}\n\n`;
        });
        
        // Totales
        message += `*💰 RESUMEN*\n`;
        message += `Subtotal: ${formatPrice(state.subtotal)}\n`;
        message += `Envío: ${state.shipping === 0 ? '¡Gratis!' : formatPrice(state.shipping)}\n`;
        message += `*Total: ${formatPrice(state.total)}*\n\n`;
        
        // Método de pago
        message += `*💳 MÉTODO DE PAGO*\n`;
        message += `${getPaymentMethodName(orderData.paymentMethod)}\n\n`;
        
        // Notas adicionales
        if (orderData.notes) {
            message += `*📝 NOTAS*\n`;
            message += `${orderData.notes}\n\n`;
        }
        
        message += `---\n`;
        message += `Pedido generado desde aguardi.com`;
        
        return message;
    }

    /**
     * Procesar pedido
     */
    async function processOrder(formData) {
        // Generar ID de pedido
        const orderId = `ORD-${Date.now()}`;
        
        // Crear objeto de pedido
        const order = {
            id: orderId,
            date: new Date().toISOString(),
            customer: {
                fullName: formData.fullName,
                email: formData.email,
                phone: formData.phone
            },
            shipping: {
                province: formData.province,
                city: formData.city,
                address: formData.address,
                postalCode: formData.postalCode,
                details: formData.addressDetails || ''
            },
            items: state.cart,
            payment: {
                method: formData.paymentMethod,
                status: 'pending'
            },
            totals: {
                subtotal: state.subtotal,
                shipping: state.shipping,
                total: state.total
            },
            notes: formData.notes || '',
            status: 'pending'
        };
        
        // Guardar pedido en localStorage (mock)
        const orders = JSON.parse(localStorage.getItem('aguardi_orders') || '[]');
        orders.push(order);
        localStorage.setItem('aguardi_orders', JSON.stringify(orders));
        
        // Limpiar carrito
        if (window.AGUARDI && window.AGUARDI.saveCart) {
            window.AGUARDI.saveCart([]);
        }
        
        return order;
    }

    /**
     * Enviar pedido por WhatsApp
     */
    function sendToWhatsApp(orderData) {
        const message = generateWhatsAppMessage(orderData);
        const encodedMessage = encodeURIComponent(message);
        const whatsappURL = `https://wa.me/${CONFIG.WHATSAPP_NUMBER}?text=${encodedMessage}`;
        
        window.open(whatsappURL, '_blank');
    }

    /**
     * Manejar envío del formulario
     */
    async function handleFormSubmit(e) {
        e.preventDefault();
        
        // Validar formulario
        if (!validateForm()) {
            // Scroll al primer error
            const firstError = document.querySelector('.form-error.show');
            if (firstError) {
                firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
            return;
        }
        
        // Recopilar datos del formulario
        const formData = {
            fullName: elements.fullName.value.trim(),
            email: elements.email.value.trim(),
            phone: elements.phone.value.trim(),
            province: elements.province.value,
            city: elements.city.value.trim(),
            address: elements.address.value.trim(),
            postalCode: elements.postalCode.value.trim(),
            addressDetails: elements.addressDetails.value.trim(),
            paymentMethod: getSelectedPaymentMethod(),
            notes: elements.notes.value.trim()
        };
        
        try {
            // Procesar pedido
            const order = await processOrder(formData);
            
            // Enviar por WhatsApp
            sendToWhatsApp(formData);
            
            // Mostrar notificación
            if (window.AGUARDI && window.AGUARDI.showNotification) {
                window.AGUARDI.showNotification('¡Pedido confirmado! Te contactaremos pronto', 'success');
            }
            
            // Redirigir a página de confirmación después de un delay
            setTimeout(() => {
                window.location.href = 'confirmation.html';
            }, 2000);
            
        } catch (error) {
            console.error('Error al procesar pedido:', error);
            if (window.AGUARDI && window.AGUARDI.showNotification) {
                window.AGUARDI.showNotification('Error al procesar el pedido. Intenta nuevamente.', 'error');
            }
        }
    }

    // ===== EVENT LISTENERS =====

    /**
     * Inicializar event listeners
     */
    function initEventListeners() {
        // Form submit
        elements.checkoutForm.addEventListener('submit', handleFormSubmit);
        
        // Limpiar errores al escribir
        const inputFields = [
            elements.fullName,
            elements.email,
            elements.phone,
            elements.province,
            elements.city,
            elements.address,
            elements.postalCode
        ];
        
        inputFields.forEach(field => {
            if (field) {
                field.addEventListener('input', () => {
                    clearFieldError(field.id);
                });
            }
        });
        
        // Términos
        if (elements.terms) {
            elements.terms.addEventListener('change', () => {
                clearFieldError('terms');
            });
        }
    }

    // ===== INICIALIZACIÓN =====

    /**
     * Inicializar checkout
     */
    function init() {
        loadCart();
        initEventListeners();
        
        console.log('Checkout AGUARDI inicializado');
    }

    // Inicializar cuando el DOM esté listo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();