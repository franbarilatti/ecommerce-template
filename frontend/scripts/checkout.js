// FILE: scripts/checkout.js
// Propósito: Lógica del checkout
// Descripción: Validación de formulario, cálculo de totales, procesamiento de pedido
// Uso: Importar en checkout.html después de main.js


// ============================================
// FILE: frontend/scripts/checkout.js (ACTUALIZADO)
// Propósito: Proceso de checkout conectado con el backend
// ============================================

let selectedAddress = null;
let shippingCost = 0;

// ========================================
// CARGAR DATOS INICIALES
// ========================================

async function loadCheckoutData() {
    // Verificar que hay productos en el carrito
    const cart = API.cart.getCart();
    if (cart.length === 0) {
        window.location.href = '/cart.html';
        return;
    }

    // Validar stock con el backend
    await validateCartStock();

    // Cargar resumen de orden
    loadOrderSummary();

    // Cargar direcciones guardadas
    await loadSavedAddresses();

    // Cargar información del usuario
    await loadUserInfo();
}

// ========================================
// VALIDAR STOCK
// ========================================

async function validateCartStock() {
    try {
        const validation = await API.cart.validateStock();

        if (!validation.valid) {
            // Hay productos sin stock suficiente
            let message = 'Algunos productos no tienen stock suficiente:\n\n';
            
            validation.invalidItems.forEach(item => {
                message += `- ${item.id}: solicitaste ${item.requestedQuantity}, disponible: ${item.availableStock || 0}\n`;
            });

            alert(message);
            window.location.href = '/cart.html';
        }
    } catch (error) {
        console.error('Error validando stock:', error);
        showNotification('Error al validar disponibilidad de productos', 'error');
    }
}

// ========================================
// CARGAR INFO DEL USUARIO
// ========================================

async function loadUserInfo() {
    try {
        const user = await API.auth.getCurrentUser();
        
        // Prellenar datos del formulario
        document.getElementById('firstName').value = user.firstName || '';
        document.getElementById('lastName').value = user.lastName || '';
        document.getElementById('email').value = user.email || '';
        document.getElementById('phone').value = user.phone || '';

    } catch (error) {
        console.error('Error cargando usuario:', error);
    }
}

// ========================================
// DIRECCIONES GUARDADAS
// ========================================

async function loadSavedAddresses() {
    try {
        const addresses = await API.users.getAddresses();
        const savedAddressesContainer = document.getElementById('savedAddresses');

        if (addresses.length > 0 && savedAddressesContainer) {
            savedAddressesContainer.innerHTML = `
                <h3>Direcciones Guardadas</h3>
                <div class="addresses-list">
                    ${addresses.map(address => `
                        <div class="address-card ${address.isDefault ? 'default' : ''}" 
                             onclick="selectSavedAddress(${address.id})">
                            <div class="address-info">
                                <strong>${address.street}, ${address.number}</strong>
                                ${address.floor ? `<p>Piso ${address.floor} ${address.apartment ? ', Depto ' + address.apartment : ''}</p>` : ''}
                                <p>${address.city}, ${address.province}</p>
                                <p>CP: ${address.postalCode}</p>
                                ${address.isDefault ? '<span class="badge">Predeterminada</span>' : ''}
                            </div>
                        </div>
                    `).join('')}
                </div>
                <div style="margin-top: 15px;">
                    <button type="button" class="btn btn-secondary" onclick="showNewAddressForm()">
                        + Nueva Dirección
                    </button>
                </div>
            `;

            // Guardar direcciones para uso posterior
            window.savedAddresses = addresses;

            // Seleccionar dirección predeterminada automáticamente
            const defaultAddress = addresses.find(a => a.isDefault);
            if (defaultAddress) {
                selectSavedAddress(defaultAddress.id);
            }
        } else {
            showNewAddressForm();
        }
    } catch (error) {
        console.error('Error cargando direcciones:', error);
        showNewAddressForm();
    }
}

function selectSavedAddress(addressId) {
    selectedAddress = window.savedAddresses.find(a => a.id === addressId);

    // Marcar visualmente la dirección seleccionada
    document.querySelectorAll('.address-card').forEach(card => {
        card.classList.remove('selected');
    });
    event.target.closest('.address-card').classList.add('selected');

    // Ocultar formulario de nueva dirección
    const newAddressForm = document.getElementById('newAddressForm');
    if (newAddressForm) {
        newAddressForm.style.display = 'none';
    }

    // Calcular envío
    calculateShipping();
}

function showNewAddressForm() {
    selectedAddress = null;
    const newAddressForm = document.getElementById('newAddressForm');
    const savedAddressesContainer = document.getElementById('savedAddresses');

    if (newAddressForm) {
        newAddressForm.style.display = 'block';
    }

    // Desmarcar direcciones guardadas
    document.querySelectorAll('.address-card').forEach(card => {
        card.classList.remove('selected');
    });
}

// ========================================
// RESUMEN DE ORDEN
// ========================================

function loadOrderSummary() {
    const cart = API.cart.getCart();
    const subtotal = API.cart.getTotal();
    
    // Renderizar items
    const orderItemsEl = document.getElementById('orderItems');
    if (orderItemsEl) {
        orderItemsEl.innerHTML = cart.map(item => `
            <div class="order-item">
                <img src="${item.image || '/images/placeholder.jpg'}" alt="${item.name}">
                <div class="item-details">
                    <h4>${item.name}</h4>
                    <p>Cantidad: ${item.quantity}</p>
                </div>
                <div class="item-price">
                    $${(item.price * item.quantity).toFixed(2)}
                </div>
            </div>
        `).join('');
    }

    // Actualizar totales
    updateOrderTotals(subtotal, shippingCost);
}

function updateOrderTotals(subtotal, shipping) {
    const total = subtotal + shipping;

    document.getElementById('subtotal').textContent = `$${subtotal.toFixed(2)}`;
    document.getElementById('shipping').textContent = shipping === 0 ? 'Gratis' : `$${shipping.toFixed(2)}`;
    document.getElementById('total').textContent = `$${total.toFixed(2)}`;
}

// ========================================
// CALCULAR ENVÍO
// ========================================

async function calculateShipping() {
    try {
        let shippingInfo;

        if (selectedAddress) {
            // Usar dirección guardada
            shippingInfo = {
                street: selectedAddress.street,
                number: selectedAddress.number,
                floor: selectedAddress.floor,
                apartment: selectedAddress.apartment,
                city: selectedAddress.city,
                province: selectedAddress.province,
                postalCode: selectedAddress.postalCode,
                country: selectedAddress.country || 'Argentina'
            };
        } else {
            // Usar dirección del formulario
            shippingInfo = {
                street: document.getElementById('street')?.value,
                number: document.getElementById('number')?.value,
                floor: document.getElementById('floor')?.value,
                apartment: document.getElementById('apartment')?.value,
                city: document.getElementById('city')?.value,
                province: document.getElementById('province')?.value,
                postalCode: document.getElementById('postalCode')?.value,
                country: 'Argentina'
            };
        }

        // Llamar a la API para calcular envío
        shippingCost = await API.orders.calculateShipping(shippingInfo);

        // Actualizar totales
        const subtotal = API.cart.getTotal();
        updateOrderTotals(subtotal, shippingCost);

    } catch (error) {
        console.error('Error calculando envío:', error);
        shippingCost = 2500; // Costo default
        const subtotal = API.cart.getTotal();
        updateOrderTotals(subtotal, shippingCost);
    }
}

// ========================================
// PROCESAR CHECKOUT
// ========================================

const checkoutForm = document.getElementById('checkoutForm');
if (checkoutForm) {
    checkoutForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const submitBtn = checkoutForm.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;

        try {
            // Deshabilitar botón
            submitBtn.disabled = true;
            submitBtn.textContent = 'Procesando...';

            // Preparar datos de envío
            let shippingInfo;

            if (selectedAddress) {
                // Usar dirección guardada
                shippingInfo = {
                    recipientName: `${document.getElementById('firstName').value} ${document.getElementById('lastName').value}`,
                    recipientPhone: document.getElementById('phone').value,
                    street: selectedAddress.street,
                    number: selectedAddress.number,
                    floor: selectedAddress.floor,
                    apartment: selectedAddress.apartment,
                    city: selectedAddress.city,
                    province: selectedAddress.province,
                    postalCode: selectedAddress.postalCode,
                    country: selectedAddress.country || 'Argentina'
                };
            } else {
                // Usar nueva dirección del formulario
                shippingInfo = {
                    recipientName: `${document.getElementById('firstName').value} ${document.getElementById('lastName').value}`,
                    recipientPhone: document.getElementById('phone').value,
                    street: document.getElementById('street').value,
                    number: document.getElementById('number').value,
                    floor: document.getElementById('floor')?.value || null,
                    apartment: document.getElementById('apartment')?.value || null,
                    city: document.getElementById('city').value,
                    province: document.getElementById('province').value,
                    postalCode: document.getElementById('postalCode').value,
                    country: 'Argentina'
                };
            }

            // Preparar items de la orden
            const cart = API.cart.getCart();
            const items = cart.map(item => ({
                productId: item.id,
                quantity: item.quantity
            }));

            // Crear orden
            const orderData = {
                items: items,
                shippingInfo: shippingInfo,
                customerNotes: document.getElementById('orderNotes')?.value || null
            };

            const order = await API.orders.createOrder(orderData);

            console.log('Orden creada:', order);

            // Crear pago y obtener preferencia de MercadoPago
            const payment = await API.payments.createPayment(order.id);

            console.log('Pago creado:', payment);

            // Limpiar carrito
            API.cart.clearCart();

            // Redirigir a MercadoPago
            if (payment.initPoint) {
                window.location.href = payment.initPoint;
            } else {
                // Si no hay initPoint, ir a confirmación
                window.location.href = `/confirmation.html?orderNumber=${order.orderNumber}`;
            }

        } catch (error) {
            console.error('Error en checkout:', error);
            
            let errorMessage = 'Error al procesar la orden. Intenta nuevamente.';
            
            if (error.message) {
                errorMessage = error.message;
            }

            if (error.status === 400) {
                errorMessage = 'Datos inválidos. Verifica la información ingresada.';
            }

            if (error.message && error.message.includes('stock')) {
                errorMessage = 'Algunos productos no tienen stock suficiente. Por favor, revisa tu carrito.';
            }

            showNotification(errorMessage, 'error');

            // Re-habilitar botón
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    });
}

// ========================================
// VALIDACIÓN DE FORMULARIO
// ========================================

function setupFormValidation() {
    const requiredFields = checkoutForm?.querySelectorAll('[required]');
    
    requiredFields?.forEach(field => {
        field.addEventListener('blur', () => {
            validateField(field);
        });
    });
}

function validateField(field) {
    if (field.value.trim() === '') {
        field.classList.add('error');
        return false;
    } else {
        field.classList.remove('error');
        return true;
    }
}

// ========================================
// PROVINCIAS ARGENTINAS
// ========================================

function setupProvinceSelect() {
    const provinceSelect = document.getElementById('province');
    if (!provinceSelect) return;

    const provinces = [
        'Buenos Aires', 'CABA', 'Catamarca', 'Chaco', 'Chubut', 'Córdoba',
        'Corrientes', 'Entre Ríos', 'Formosa', 'Jujuy', 'La Pampa', 'La Rioja',
        'Mendoza', 'Misiones', 'Neuquén', 'Río Negro', 'Salta', 'San Juan',
        'San Luis', 'Santa Cruz', 'Santa Fe', 'Santiago del Estero',
        'Tierra del Fuego', 'Tucumán'
    ];

    provinceSelect.innerHTML = `
        <option value="">Seleccionar provincia</option>
        ${provinces.map(p => `<option value="${p}">${p}</option>`).join('')}
    `;
}

// ========================================
// INICIALIZACIÓN
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    // Verificar autenticación
    if (!API.token.isAuthenticated()) {
        window.location.href = '/login.html?returnUrl=/checkout.html';
        return;
    }

    // Cargar datos del checkout
    loadCheckoutData();

    // Setup provincia select
    setupProvinceSelect();

    // Setup validación
    setupFormValidation();

    // Event listener para campos de dirección (calcular envío cuando cambian)
    const addressFields = ['street', 'city', 'province', 'postalCode'];
    addressFields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field) {
            field.addEventListener('blur', calculateShipping);
        }
    });
});

// Hacer funciones disponibles globalmente
window.selectSavedAddress = selectSavedAddress;
window.showNewAddressForm = showNewAddressForm;