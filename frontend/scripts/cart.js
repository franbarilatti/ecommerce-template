// FILE: scripts/cart.js
// Propósito: Lógica del carrito de compras
// Descripción: Mostrar items, modificar cantidades, eliminar, calcular totales
// Uso: Importar en cart.html después de main.js

// ============================================
// FILE: frontend/scripts/cart.js (ACTUALIZADO)
// Propósito: Carrito de compras conectado con el backend
// ============================================

// ========================================
// CARGAR CARRITO
// ========================================

async function loadCart() {
    const cart = API.cart.getCart();
    const cartItemsEl = document.getElementById('cartItems');
    const emptyCartEl = document.getElementById('emptyCart');
    const cartSummaryEl = document.getElementById('cartSummary');

    // Si el carrito está vacío
    if (cart.length === 0) {
        if (cartItemsEl) cartItemsEl.style.display = 'none';
        if (emptyCartEl) emptyCartEl.style.display = 'block';
        if (cartSummaryEl) cartSummaryEl.style.display = 'none';
        return;
    }

    // Mostrar elementos
    if (cartItemsEl) cartItemsEl.style.display = 'block';
    if (emptyCartEl) emptyCartEl.style.display = 'none';
    if (cartSummaryEl) cartSummaryEl.style.display = 'block';

    // Validar disponibilidad de productos con el backend
    await validateAndUpdateCart(cart);

    // Renderizar items del carrito
    renderCartItems();

    // Actualizar resumen
    updateCartSummary();
}

// ========================================
// VALIDAR DISPONIBILIDAD CON BACKEND
// ========================================

async function validateAndUpdateCart(cart) {
    try {
        // Obtener información actualizada de cada producto
        const productsPromises = cart.map(item => 
            API.products.getProductById(item.id).catch(err => {
                console.error(`Error obteniendo producto ${item.id}:`, err);
                return null;
            })
        );

        const products = await Promise.all(productsPromises);

        let cartUpdated = false;
        const updatedCart = [];

        cart.forEach((item, index) => {
            const product = products[index];

            if (!product) {
                // Producto no encontrado - remover del carrito
                console.warn(`Producto ${item.id} no encontrado`);
                showNotification(`El producto "${item.name}" ya no está disponible`, 'warning');
                cartUpdated = true;
                return;
            }

            if (product.stock === 0) {
                // Sin stock - remover del carrito
                showNotification(`"${product.name}" está agotado`, 'warning');
                cartUpdated = true;
                return;
            }

            // Actualizar información del producto
            const updatedItem = {
                ...item,
                name: product.name,
                price: product.salePrice || product.price,
                image: product.mainImageUrl,
                stock: product.stock
            };

            // Si la cantidad supera el stock disponible, ajustar
            if (item.quantity > product.stock) {
                updatedItem.quantity = product.stock;
                showNotification(
                    `Cantidad de "${product.name}" ajustada a ${product.stock} (stock disponible)`,
                    'warning'
                );
                cartUpdated = true;
            }

            updatedCart.push(updatedItem);
        });

        // Si hubo cambios, actualizar carrito
        if (cartUpdated) {
            API.cart.setCart(updatedCart);
        }

    } catch (error) {
        console.error('Error validando carrito:', error);
    }
}

// ========================================
// RENDERIZAR ITEMS
// ========================================

function renderCartItems() {
    const cart = API.cart.getCart();
    const cartItemsEl = document.getElementById('cartItems');

    if (!cartItemsEl) return;

    cartItemsEl.innerHTML = cart.map(item => `
        <div class="cart-item" data-product-id="${item.id}">
            <div class="item-image">
                <img src="${item.image || '/images/placeholder.jpg'}" 
                     alt="${item.name}"
                     onerror="this.src='/images/placeholder.jpg'">
            </div>
            <div class="item-details">
                <h3>${item.name}</h3>
                <p class="item-price">$${item.price.toFixed(2)}</p>
                <p class="item-stock">
                    ${item.stock <= 5 ? `<span class="text-warning">¡Solo quedan ${item.stock}!</span>` : 
                       `Stock disponible: ${item.stock}`}
                </p>
            </div>
            <div class="item-quantity">
                <button class="btn-quantity" onclick="updateItemQuantity(${item.id}, ${item.quantity - 1})">
                    <i class="fas fa-minus"></i>
                </button>
                <input type="number" 
                       value="${item.quantity}" 
                       min="1" 
                       max="${item.stock}"
                       onchange="updateItemQuantity(${item.id}, this.value)"
                       class="quantity-input">
                <button class="btn-quantity" 
                        onclick="updateItemQuantity(${item.id}, ${item.quantity + 1})"
                        ${item.quantity >= item.stock ? 'disabled' : ''}>
                    <i class="fas fa-plus"></i>
                </button>
            </div>
            <div class="item-total">
                <p class="total-price">$${(item.price * item.quantity).toFixed(2)}</p>
                <button class="btn-remove" onclick="removeItemFromCart(${item.id})">
                    <i class="fas fa-trash"></i> Eliminar
                </button>
            </div>
        </div>
    `).join('');
}

// ========================================
// ACTUALIZAR CANTIDAD
// ========================================

function updateItemQuantity(productId, newQuantity) {
    const cart = API.cart.getCart();
    const item = cart.find(i => i.id === productId);

    if (!item) return;

    // Validar cantidad
    newQuantity = parseInt(newQuantity);

    if (newQuantity < 1) {
        removeItemFromCart(productId);
        return;
    }

    if (newQuantity > item.stock) {
        showNotification(`Solo hay ${item.stock} unidades disponibles`, 'warning');
        newQuantity = item.stock;
    }

    // Actualizar cantidad
    API.cart.updateQuantity(productId, newQuantity);

    // Re-renderizar
    renderCartItems();
    updateCartSummary();
}

// ========================================
// ELIMINAR ITEM
// ========================================

function removeItemFromCart(productId) {
    if (confirm('¿Eliminar este producto del carrito?')) {
        API.cart.removeItem(productId);
        
        // Re-cargar carrito
        loadCart();
        
        showNotification('Producto eliminado del carrito', 'info');
    }
}

// ========================================
// LIMPIAR CARRITO
// ========================================

function clearCart() {
    if (confirm('¿Estás seguro que deseas vaciar el carrito?')) {
        API.cart.clearCart();
        loadCart();
        showNotification('Carrito vaciado', 'info');
    }
}

// ========================================
// RESUMEN DEL CARRITO
// ========================================

function updateCartSummary() {
    const cart = API.cart.getCart();
    const subtotal = API.cart.getTotal();
    const itemCount = API.cart.getItemCount();

    // Calcular envío (simplificado - el cálculo real se hace en checkout)
    const freeShippingThreshold = 50000; // $50,000
    const shippingCost = subtotal >= freeShippingThreshold ? 0 : 2500;

    const total = subtotal + shippingCost;

    // Actualizar elementos del DOM
    const itemCountEl = document.getElementById('itemCount');
    const subtotalEl = document.getElementById('subtotal');
    const shippingEl = document.getElementById('shipping');
    const totalEl = document.getElementById('total');
    const freeShippingNoticeEl = document.getElementById('freeShippingNotice');

    if (itemCountEl) {
        itemCountEl.textContent = `${itemCount} ${itemCount === 1 ? 'producto' : 'productos'}`;
    }

    if (subtotalEl) {
        subtotalEl.textContent = `$${subtotal.toFixed(2)}`;
    }

    if (shippingEl) {
        if (shippingCost === 0) {
            shippingEl.innerHTML = '<span class="text-success">¡Gratis!</span>';
        } else {
            shippingEl.textContent = `$${shippingCost.toFixed(2)}`;
        }
    }

    if (totalEl) {
        totalEl.textContent = `$${total.toFixed(2)}`;
    }

    // Mostrar aviso de envío gratis
    if (freeShippingNoticeEl) {
        if (subtotal < freeShippingThreshold) {
            const remaining = freeShippingThreshold - subtotal;
            freeShippingNoticeEl.innerHTML = `
                <div class="shipping-notice">
                    <i class="fas fa-truck"></i>
                    Te faltan <strong>$${remaining.toFixed(2)}</strong> para envío gratis
                </div>
            `;
            freeShippingNoticeEl.style.display = 'block';
        } else {
            freeShippingNoticeEl.innerHTML = `
                <div class="shipping-notice success">
                    <i class="fas fa-check-circle"></i>
                    ¡Tienes envío gratis!
                </div>
            `;
            freeShippingNoticeEl.style.display = 'block';
        }
    }
}

// ========================================
// IR A CHECKOUT
// ========================================

async function goToCheckout() {
    const cart = API.cart.getCart();

    if (cart.length === 0) {
        showNotification('El carrito está vacío', 'warning');
        return;
    }

    // Verificar autenticación
    if (!API.token.isAuthenticated()) {
        // Guardar returnUrl para volver después del login
        window.location.href = '/login.html?returnUrl=/checkout.html';
        return;
    }

    // Validar stock una última vez antes de ir a checkout
    const validation = await API.cart.validateStock();

    if (!validation.valid) {
        let message = 'Algunos productos no están disponibles:\n\n';
        validation.invalidItems.forEach(item => {
            const product = cart.find(p => p.id === item.id);
            message += `- ${product?.name || item.id}: `;
            if (item.error) {
                message += item.error;
            } else {
                message += `Solicitaste ${item.requestedQuantity}, disponible: ${item.availableStock}`;
            }
            message += '\n';
        });

        alert(message);
        
        // Recargar carrito para actualizar
        await loadCart();
        return;
    }

    // Todo OK, ir a checkout
    window.location.href = '/checkout.html';
}

// ========================================
// CONTINUAR COMPRANDO
// ========================================

function continueShopping() {
    window.location.href = '/catalog.html';
}

// ========================================
// APLICAR CUPÓN (FUTURO)
// ========================================

function applyCoupon() {
    const couponInput = document.getElementById('couponCode');
    if (!couponInput) return;

    const code = couponInput.value.trim();

    if (!code) {
        showNotification('Ingresa un código de cupón', 'warning');
        return;
    }

    // TODO: Implementar validación de cupones con el backend
    showNotification('Funcionalidad de cupones próximamente', 'info');
}

// ========================================
// PRODUCTOS RECOMENDADOS
// ========================================

async function loadRecommendedProducts() {
    try {
        // Cargar productos destacados o nuevos
        const response = await API.products.getProducts({
            page: 0,
            size: 4,
            sortBy: 'createdAt',
            sortDir: 'DESC'
        });

        const recommendedEl = document.getElementById('recommendedProducts');
        if (!recommendedEl || !response.content) return;

        recommendedEl.innerHTML = `
            <h3>También te puede interesar</h3>
            <div class="recommended-grid">
                ${response.content.map(product => `
                    <div class="product-card-small">
                        <img src="${product.mainImageUrl || '/images/placeholder.jpg'}" 
                             alt="${product.name}">
                        <h4>${product.name}</h4>
                        <p class="price">$${(product.salePrice || product.price).toFixed(2)}</p>
                        <button class="btn btn-sm" onclick="window.location.href='/product.html?id=${product.id}'">
                            Ver
                        </button>
                    </div>
                `).join('')}
            </div>
        `;

    } catch (error) {
        console.error('Error cargando productos recomendados:', error);
    }
}

// ========================================
// INICIALIZACIÓN
// ========================================

document.addEventListener('DOMContentLoaded', async () => {
    // Cargar carrito
    await loadCart();

    // Cargar productos recomendados
    await loadRecommendedProducts();

    // Event listeners
    const checkoutBtn = document.getElementById('checkoutBtn');
    const continueShoppingBtn = document.getElementById('continueShoppingBtn');
    const clearCartBtn = document.getElementById('clearCartBtn');
    const applyCouponBtn = document.getElementById('applyCouponBtn');

    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', goToCheckout);
    }

    if (continueShoppingBtn) {
        continueShoppingBtn.addEventListener('click', continueShopping);
    }

    if (clearCartBtn) {
        clearCartBtn.addEventListener('click', clearCart);
    }

    if (applyCouponBtn) {
        applyCouponBtn.addEventListener('click', applyCoupon);
    }
});

// Hacer funciones disponibles globalmente
window.updateItemQuantity = updateItemQuantity;
window.removeItemFromCart = removeItemFromCart;
window.clearCart = clearCart;
window.goToCheckout = goToCheckout;
window.continueShopping = continueShopping;
window.applyCoupon = applyCoupon;