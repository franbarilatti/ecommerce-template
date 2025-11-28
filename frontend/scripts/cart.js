// FILE: scripts/cart.js
// Propósito: Lógica del carrito de compras
// Descripción: Mostrar items, modificar cantidades, eliminar, calcular totales
// Uso: Importar en cart.html después de main.js

(function() {
    'use strict';

    // ===== CONFIGURACIÓN =====
    const CONFIG = {
        WHATSAPP_NUMBER: '5492231234567', // Número de WhatsApp
        MIN_QUANTITY: 1,
        SHIPPING_THRESHOLD: 50000, // Envío gratis sobre este monto (en centavos)
        SHIPPING_COST: 2000 // Costo de envío estándar
    };

    // ===== ESTADO =====
    let state = {
        cart: [],
        subtotal: 0,
        shipping: 0,
        total: 0
    };

    // ===== ELEMENTOS DEL DOM =====
    const elements = {
        // Estados
        emptyCart: document.getElementById('emptyCart'),
        cartContent: document.getElementById('cartContent'),
        
        // Header
        itemCount: document.getElementById('itemCount'),
        
        // Items
        cartItemsList: document.getElementById('cartItemsList'),
        clearCartBtn: document.getElementById('clearCartBtn'),
        
        // Summary
        subtotalEl: document.getElementById('subtotal'),
        shippingEl: document.getElementById('shipping'),
        totalEl: document.getElementById('total'),
        checkoutBtn: document.getElementById('checkoutBtn'),
        whatsappOrderBtn: document.getElementById('whatsappOrderBtn'),
        
        // Recommended
        recommendedSection: document.getElementById('recommendedSection'),
        recommendedProducts: document.getElementById('recommendedProducts'),
        
        // Modal
        clearCartModal: document.getElementById('clearCartModal'),
        modalCloseBtn: document.getElementById('modalCloseBtn'),
        modalCancelBtn: document.getElementById('modalCancelBtn'),
        modalConfirmBtn: document.getElementById('modalConfirmBtn')
    };

    // ===== UTILIDADES =====

    /**
     * Obtener ícono según categoría
     * @param {string} category - Categoría del producto
     * @returns {string} Emoji del ícono
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
     * Obtener traducción de categoría
     * @param {string} category - Categoría en inglés
     * @returns {string} Categoría en español
     */
    function getCategoryName(category) {
        const names = {
            'baby': 'Bebé',
            'boy': 'Niño',
            'girl': 'Niña',
            'party': 'Fiesta',
            'accessories': 'Accesorios'
        };
        return names[category] || category;
    }

    /**
     * Formatear precio a moneda argentina
     * @param {number} price - Precio en pesos
     * @returns {string} Precio formateado
     */
    function formatPrice(price) {
        return `$${price.toLocaleString('es-AR')}`;
    }

    // ===== GESTIÓN DEL CARRITO =====

    /**
     * Cargar carrito desde localStorage
     */
    function loadCart() {
        if (window.AGUARDI && window.AGUARDI.getCart) {
            state.cart = window.AGUARDI.getCart();
        } else {
            state.cart = [];
        }
        
        calculateTotals();
        renderCart();
    }

    /**
     * Guardar carrito en localStorage
     */
    function saveCart() {
        if (window.AGUARDI && window.AGUARDI.saveCart) {
            window.AGUARDI.saveCart(state.cart);
        }
        calculateTotals();
        renderCart();
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
        if (state.subtotal >= CONFIG.SHIPPING_THRESHOLD) {
            state.shipping = 0;
        } else if (state.subtotal > 0) {
            state.shipping = CONFIG.SHIPPING_COST;
        } else {
            state.shipping = 0;
        }
        
        // Total
        state.total = state.subtotal + state.shipping;
    }

    /**
     * Actualizar cantidad de un item
     * @param {number} productId - ID del producto
     * @param {number} newQuantity - Nueva cantidad
     */
    function updateQuantity(productId, newQuantity) {
        const item = state.cart.find(i => i.id === productId);
        
        if (!item) return;
        
        // Validar cantidad
        if (newQuantity < CONFIG.MIN_QUANTITY) {
            newQuantity = CONFIG.MIN_QUANTITY;
        }
        
        if (newQuantity > item.stock) {
            newQuantity = item.stock;
            showNotification('Cantidad máxima disponible', 'warning');
        }
        
        item.quantity = newQuantity;
        saveCart();
    }

    /**
     * Eliminar item del carrito
     * @param {number} productId - ID del producto
     */
    function removeItem(productId) {
        // Agregar clase de animación
        const itemElement = document.querySelector(`[data-item-id="${productId}"]`);
        if (itemElement) {
            itemElement.classList.add('removing');
            
            // Esperar a que termine la animación
            setTimeout(() => {
                state.cart = state.cart.filter(item => item.id !== productId);
                saveCart();
                showNotification('Producto eliminado del carrito', 'success');
            }, 300);
        } else {
            state.cart = state.cart.filter(item => item.id !== productId);
            saveCart();
        }
    }

    /**
     * Vaciar carrito completamente
     */
    function clearCart() {
        state.cart = [];
        saveCart();
        closeModal();
        showNotification('Carrito vaciado', 'success');
    }

    // ===== RENDERIZADO =====

    /**
     * Renderizar carrito completo
     */
    function renderCart() {
        updateItemCount();
        
        if (state.cart.length === 0) {
            showEmptyState();
        } else {
            showCartContent();
            renderCartItems();
            renderSummary();
            loadRecommendedProducts();
        }
    }

    /**
     * Actualizar contador de items
     */
    function updateItemCount() {
        const totalItems = state.cart.reduce((sum, item) => sum + item.quantity, 0);
        elements.itemCount.textContent = `${totalItems} producto${totalItems !== 1 ? 's' : ''}`;
    }

    /**
     * Mostrar estado vacío
     */
    function showEmptyState() {
        elements.emptyCart.style.display = 'flex';
        elements.cartContent.style.display = 'none';
        elements.recommendedSection.style.display = 'none';
    }

    /**
     * Mostrar contenido del carrito
     */
    function showCartContent() {
        elements.emptyCart.style.display = 'none';
        elements.cartContent.style.display = 'block';
    }

    /**
     * Renderizar items del carrito
     */
    function renderCartItems() {
        elements.cartItemsList.innerHTML = '';
        
        state.cart.forEach((item, index) => {
            const itemElement = createCartItemElement(item, index);
            elements.cartItemsList.appendChild(itemElement);
        });
    }

    /**
     * Crear elemento de item del carrito
     * @param {Object} item - Item del carrito
     * @param {number} index - Índice del item
     * @returns {HTMLElement} Elemento del item
     */
    function createCartItemElement(item, index) {
        const itemDiv = document.createElement('div');
        itemDiv.className = 'cart-item';
        itemDiv.setAttribute('data-item-id', item.id);
        
        // Calcular subtotal del item
        const itemSubtotal = item.price * item.quantity;
        
        // Verificar stock bajo
        const stockWarning = item.quantity >= item.stock * 0.8;
        
        itemDiv.innerHTML = `
            <a href="product.html?id=${item.id}" class="item-image">
                <div class="image-placeholder">
                    <span class="placeholder-icon">${getCategoryIcon(item.category)}</span>
                </div>
            </a>
            <div class="item-info">
                <a href="product.html?id=${item.id}" class="item-name">${item.name}</a>
                <span class="item-category">${getCategoryName(item.category)}</span>
                <span class="item-price">${formatPrice(item.price)} c/u</span>
                ${stockWarning ? `<span class="item-stock low-stock">Últimas ${item.stock} unidades</span>` : ''}
            </div>
            <div class="quantity-controls">
                <button 
                    class="quantity-btn decrease-btn" 
                    data-product-id="${item.id}"
                    aria-label="Disminuir cantidad"
                    ${item.quantity <= CONFIG.MIN_QUANTITY ? 'disabled' : ''}
                >-</button>
                <span class="quantity-display">${item.quantity}</span>
                <button 
                    class="quantity-btn increase-btn" 
                    data-product-id="${item.id}"
                    aria-label="Aumentar cantidad"
                    ${item.quantity >= item.stock ? 'disabled' : ''}
                >+</button>
            </div>
            <div class="item-subtotal">
                <span class="subtotal-label">Subtotal:</span>
                <span class="subtotal-value">${formatPrice(itemSubtotal)}</span>
            </div>
            <button 
                class="btn-remove" 
                data-product-id="${item.id}"
                aria-label="Eliminar producto"
            >
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
            </button>
        `;
        
        // Event listeners
        const decreaseBtn = itemDiv.querySelector('.decrease-btn');
        const increaseBtn = itemDiv.querySelector('.increase-btn');
        const removeBtn = itemDiv.querySelector('.btn-remove');
        
        decreaseBtn.addEventListener('click', () => {
            updateQuantity(item.id, item.quantity - 1);
        });
        
        increaseBtn.addEventListener('click', () => {
            updateQuantity(item.id, item.quantity + 1);
        });
        
        removeBtn.addEventListener('click', () => {
            removeItem(item.id);
        });
        
        return itemDiv;
    }

    /**
     * Renderizar resumen del pedido
     */
    function renderSummary() {
        elements.subtotalEl.textContent = formatPrice(state.subtotal);
        
        if (state.shipping === 0 && state.subtotal > 0) {
            elements.shippingEl.textContent = '¡Gratis!';
            elements.shippingEl.style.color = 'var(--success)';
            elements.shippingEl.style.fontWeight = '700';
        } else if (state.shipping > 0) {
            elements.shippingEl.textContent = formatPrice(state.shipping);
            elements.shippingEl.style.color = '';
            elements.shippingEl.style.fontWeight = '';
        } else {
            elements.shippingEl.textContent = 'A calcular';
            elements.shippingEl.style.color = '';
            elements.shippingEl.style.fontWeight = '';
        }
        
        elements.totalEl.textContent = formatPrice(state.total);
    }

    /**
     * Cargar productos recomendados
     */
    function loadRecommendedProducts() {
        // Por ahora, no mostrar productos recomendados
        // En producción, aquí se cargarían productos relacionados desde la API
        elements.recommendedSection.style.display = 'none';
    }

    // ===== ACCIONES =====

    /**
     * Proceder al checkout
     */
    function handleCheckout() {
        if (state.cart.length === 0) {
            showNotification('El carrito está vacío', 'warning');
            return;
        }
        
        // Redirigir a checkout
        window.location.href = 'checkout.html';
    }

    /**
     * Generar mensaje de WhatsApp
     */
    function generateWhatsAppMessage() {
        let message = `¡Hola! Me gustaría hacer el siguiente pedido:\n\n`;
        
        state.cart.forEach((item, index) => {
            message += `${index + 1}. *${item.name}*\n`;
            message += `   Cantidad: ${item.quantity}\n`;
            message += `   Precio: ${formatPrice(item.price)} c/u\n`;
            message += `   Subtotal: ${formatPrice(item.price * item.quantity)}\n\n`;
        });
        
        message += `*Subtotal:* ${formatPrice(state.subtotal)}\n`;
        message += `*Envío:* ${state.shipping === 0 ? '¡Gratis!' : formatPrice(state.shipping)}\n`;
        message += `*Total:* ${formatPrice(state.total)}\n\n`;
        message += `¿Podrían confirmarme la disponibilidad?`;
        
        return message;
    }

    /**
     * Abrir WhatsApp con pedido
     */
    function handleWhatsAppOrder() {
        if (state.cart.length === 0) {
            showNotification('El carrito está vacío', 'warning');
            return;
        }
        
        const message = generateWhatsAppMessage();
        const encodedMessage = encodeURIComponent(message);
        const whatsappURL = `https://wa.me/${CONFIG.WHATSAPP_NUMBER}?text=${encodedMessage}`;
        
        window.open(whatsappURL, '_blank');
    }

    /**
     * Mostrar notificación
     * @param {string} message - Mensaje
     * @param {string} type - Tipo de notificación
     */
    function showNotification(message, type) {
        if (window.AGUARDI && window.AGUARDI.showNotification) {
            window.AGUARDI.showNotification(message, type);
        } else {
            console.log(`[${type}] ${message}`);
        }
    }

    // ===== MODAL =====

    /**
     * Abrir modal de confirmación
     */
    function openModal() {
        elements.clearCartModal.classList.add('active');
        document.body.style.overflow = 'hidden';
    }

    /**
     * Cerrar modal
     */
    function closeModal() {
        elements.clearCartModal.classList.remove('active');
        document.body.style.overflow = '';
    }

    // ===== EVENT LISTENERS =====

    /**
     * Inicializar event listeners
     */
    function initEventListeners() {
        // Clear cart
        elements.clearCartBtn.addEventListener('click', openModal);
        
        // Checkout
        elements.checkoutBtn.addEventListener('click', handleCheckout);
        
        // WhatsApp
        elements.whatsappOrderBtn.addEventListener('click', handleWhatsAppOrder);
        
        // Modal
        elements.modalCloseBtn.addEventListener('click', closeModal);
        elements.modalCancelBtn.addEventListener('click', closeModal);
        elements.modalConfirmBtn.addEventListener('click', clearCart);
        
        // Cerrar modal al hacer click en backdrop
        const modalBackdrop = elements.clearCartModal.querySelector('.modal-backdrop');
        if (modalBackdrop) {
            modalBackdrop.addEventListener('click', closeModal);
        }
        
        // Cerrar modal con ESC
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && elements.clearCartModal.classList.contains('active')) {
                closeModal();
            }
        });
        
        // Escuchar cambios en el carrito desde otras pestañas
        window.addEventListener('storage', (e) => {
            if (e.key === 'aguardi_cart') {
                loadCart();
            }
        });
    }

    // ===== INICIALIZACIÓN =====

    /**
     * Inicializar página de carrito
     */
    function init() {
        initEventListeners();
        loadCart();
        
        console.log('Carrito AGUARDI inicializado');
    }

    // Inicializar cuando el DOM esté listo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();