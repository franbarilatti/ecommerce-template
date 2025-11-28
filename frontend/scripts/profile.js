// FILE: scripts/profile.js
// Propósito: Lógica de la página de perfil de usuario
// Descripción: Ver/editar datos, historial de pedidos, cambiar contraseña
// Uso: Importar en profile.html después de main.js

(function() {
    'use strict';

    // ===== VERIFICACIÓN DE ACCESO =====
    function checkUserAccess() {
        const user = JSON.parse(localStorage.getItem('aguardi_user') || 'null');
        
        if (!user || !user.logged) {
            window.location.href = 'login.html?redirect=profile.html';
            return false;
        }
        
        return user;
    }

    const currentUser = checkUserAccess();
    if (!currentUser) return;

    // ===== ESTADO =====
    let state = {
        currentTab: 'personal-info',
        user: null,
        orders: [],
        originalUserData: null
    };

    // ===== ELEMENTOS DEL DOM =====
    const elements = {
        // Sidebar
        userInitial: document.getElementById('userInitial'),
        userName: document.getElementById('userName'),
        userEmail: document.getElementById('userEmail'),
        profileNavLinks: document.querySelectorAll('.profile-nav-link'),
        logoutBtn: document.getElementById('logoutBtn'),
        ordersBadge: document.getElementById('ordersBadge'),
        
        // Tabs
        profileTabs: document.querySelectorAll('.profile-tab'),
        
        // Personal Info
        personalInfoForm: document.getElementById('personalInfoForm'),
        fullNameInput: document.getElementById('fullName'),
        emailInput: document.getElementById('email'),
        phoneInput: document.getElementById('phone'),
        cancelEdit: document.getElementById('cancelEdit'),
        
        // Orders
        ordersList: document.getElementById('ordersList'),
        ordersEmpty: document.getElementById('ordersEmpty'),
        
        // Security
        passwordForm: document.getElementById('passwordForm'),
        currentPassword: document.getElementById('currentPassword'),
        newPassword: document.getElementById('newPassword'),
        confirmNewPassword: document.getElementById('confirmNewPassword'),
        deleteAccountBtn: document.getElementById('deleteAccountBtn'),
        
        // Modal
        orderDetailModal: document.getElementById('orderDetailModal'),
        orderDetailBody: document.getElementById('orderDetailBody'),
        orderDetailClose: document.getElementById('orderDetailClose'),
        orderDetailClose2: document.getElementById('orderDetailClose2')
    };

    // ===== UTILIDADES =====
    
    function formatPrice(price) {
        return `$${price.toLocaleString('es-AR')}`;
    }

    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('es-AR', { 
            day: '2-digit', 
            month: 'long', 
            year: 'numeric' 
        });
    }

    function showNotification(message, type) {
        if (window.AGUARDI && window.AGUARDI.showNotification) {
            window.AGUARDI.showNotification(message, type);
        }
    }

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

    // ===== NAVEGACIÓN DE TABS =====
    
    function switchTab(tabName) {
        // Actualizar nav links
        elements.profileNavLinks.forEach(link => {
            link.classList.remove('active');
            if (link.dataset.tab === tabName) {
                link.classList.add('active');
            }
        });
        
        // Actualizar tabs
        elements.profileTabs.forEach(tab => {
            tab.classList.remove('active');
            if (tab.id === `${tabName}-tab`) {
                tab.classList.add('active');
            }
        });
        
        state.currentTab = tabName;
    }

    // ===== CARGA DE DATOS =====
    
    function loadUserData() {
        // Cargar usuario completo desde la lista de usuarios
        const users = JSON.parse(localStorage.getItem('aguardi_users') || '[]');
        state.user = users.find(u => u.id === currentUser.id);
        
        if (!state.user) {
            state.user = currentUser;
        }
        
        // Guardar copia original para cancelar edición
        state.originalUserData = { ...state.user };
        
        // Actualizar UI del sidebar
        const initial = state.user.fullName ? state.user.fullName.charAt(0).toUpperCase() : 'U';
        elements.userInitial.textContent = initial;
        elements.userName.textContent = state.user.fullName;
        elements.userEmail.textContent = state.user.email;
        
        // Llenar formulario
        elements.fullNameInput.value = state.user.fullName || '';
        elements.emailInput.value = state.user.email || '';
        elements.phoneInput.value = state.user.phone || '';
    }

    function loadOrders() {
        const allOrders = JSON.parse(localStorage.getItem('aguardi_orders') || '[]');
        
        // Filtrar pedidos del usuario actual
        state.orders = allOrders.filter(order => {
            return order.customer && order.customer.email === state.user.email;
        });
        
        // Actualizar badge
        const pendingCount = state.orders.filter(o => o.status === 'pending').length;
        elements.ordersBadge.textContent = pendingCount;
        
        renderOrders();
    }

    // ===== INFORMACIÓN PERSONAL =====
    
    function handlePersonalInfoSubmit(e) {
        e.preventDefault();
        
        const fullName = elements.fullNameInput.value.trim();
        const phone = elements.phoneInput.value.trim();
        
        if (!fullName || fullName.length < 3) {
            showNotification('El nombre debe tener al menos 3 caracteres', 'error');
            return;
        }
        
        if (!phone) {
            showNotification('El teléfono es obligatorio', 'error');
            return;
        }
        
        // Actualizar datos del usuario
        state.user.fullName = fullName;
        state.user.phone = phone;
        
        // Actualizar en la lista de usuarios
        const users = JSON.parse(localStorage.getItem('aguardi_users') || '[]');
        const userIndex = users.findIndex(u => u.id === state.user.id);
        
        if (userIndex !== -1) {
            users[userIndex] = { ...users[userIndex], ...state.user };
            localStorage.setItem('aguardi_users', JSON.stringify(users));
        }
        
        // Actualizar sesión
        const session = JSON.parse(localStorage.getItem('aguardi_user'));
        if (session) {
            session.fullName = fullName;
            session.phone = phone;
            localStorage.setItem('aguardi_user', JSON.stringify(session));
        }
        
        // Actualizar copia original
        state.originalUserData = { ...state.user };
        
        // Actualizar UI
        loadUserData();
        
        showNotification('Datos actualizados correctamente', 'success');
    }

    function cancelEdit() {
        // Restaurar datos originales
        state.user = { ...state.originalUserData };
        
        // Recargar formulario
        elements.fullNameInput.value = state.user.fullName || '';
        elements.phoneInput.value = state.user.phone || '';
        
        showNotification('Cambios descartados', 'info');
    }

    // ===== PEDIDOS =====
    
    function renderOrders() {
        if (state.orders.length === 0) {
            elements.ordersList.style.display = 'none';
            elements.ordersEmpty.style.display = 'flex';
            return;
        }
        
        elements.ordersList.style.display = 'flex';
        elements.ordersEmpty.style.display = 'none';
        
        elements.ordersList.innerHTML = '';
        
        state.orders.forEach(order => {
            const orderCard = createOrderCard(order);
            elements.ordersList.appendChild(orderCard);
        });
    }

    function createOrderCard(order) {
        const div = document.createElement('div');
        div.className = 'order-card';
        
        const statusClass = order.status === 'pending' ? 'pending' : 'completed';
        const statusText = order.status === 'pending' ? 'Pendiente' : 'Completado';
        
        const orderIdShort = order.id.split('-')[1] || order.id;
        
        div.innerHTML = `
            <div class="order-header">
                <div>
                    <div class="order-id">Pedido #${orderIdShort}</div>
                    <div class="order-date">${formatDate(order.date)}</div>
                </div>
                <span class="order-status ${statusClass}">${statusText}</span>
            </div>
            
            <div class="order-items">
                ${order.items.slice(0, 3).map(item => `
                    <div class="order-item">
                        <span>${getCategoryIcon(item.category)} ${item.name} x${item.quantity}</span>
                        <span>${formatPrice(item.price * item.quantity)}</span>
                    </div>
                `).join('')}
                ${order.items.length > 3 ? `<div class="order-item"><span>+ ${order.items.length - 3} producto(s) más</span></div>` : ''}
            </div>
            
            <div class="order-footer">
                <div class="order-total">Total: ${formatPrice(order.totals.total)}</div>
                <button class="btn-view-order" onclick="window.viewOrderDetail('${order.id}')">Ver Detalle</button>
            </div>
        `;
        
        return div;
    }

    function viewOrderDetail(orderId) {
        const order = state.orders.find(o => o.id === orderId);
        if (!order) return;
        
        const orderIdShort = order.id.split('-')[1] || order.id;
        
        elements.orderDetailBody.innerHTML = `
            <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                <div>
                    <h4 style="font-weight: 700; margin-bottom: 0.5rem;">Información del Pedido</h4>
                    <p><strong>ID:</strong> #${orderIdShort}</p>
                    <p><strong>Fecha:</strong> ${formatDate(order.date)}</p>
                    <p><strong>Estado:</strong> ${order.status === 'pending' ? 'Pendiente' : 'Completado'}</p>
                </div>
                
                <div>
                    <h4 style="font-weight: 700; margin-bottom: 0.5rem;">Datos de Contacto</h4>
                    <p><strong>Nombre:</strong> ${order.customer.fullName}</p>
                    <p><strong>Email:</strong> ${order.customer.email}</p>
                    <p><strong>Teléfono:</strong> ${order.customer.phone}</p>
                </div>
                
                <div>
                    <h4 style="font-weight: 700; margin-bottom: 0.5rem;">Dirección de Envío</h4>
                    <p>${order.shipping.address}</p>
                    <p>${order.shipping.city}, ${order.shipping.province}</p>
                    <p>CP: ${order.shipping.postalCode}</p>
                    ${order.shipping.details ? `<p>${order.shipping.details}</p>` : ''}
                </div>
                
                <div>
                    <h4 style="font-weight: 700; margin-bottom: 0.5rem;">Productos</h4>
                    ${order.items.map(item => `
                        <div style="display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid #E5E7EB;">
                            <span>${item.name} x${item.quantity}</span>
                            <span>${formatPrice(item.price * item.quantity)}</span>
                        </div>
                    `).join('')}
                </div>
                
                <div>
                    <h4 style="font-weight: 700; margin-bottom: 0.5rem;">Resumen</h4>
                    <div style="display: flex; justify-content: space-between; padding: 0.5rem 0;">
                        <span>Subtotal:</span>
                        <span>${formatPrice(order.totals.subtotal)}</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; padding: 0.5rem 0;">
                        <span>Envío:</span>
                        <span>${order.totals.shipping === 0 ? '¡Gratis!' : formatPrice(order.totals.shipping)}</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; padding: 0.5rem 0; border-top: 2px solid #E5E7EB; font-weight: 700; font-size: 1.25rem; color: var(--dark-gold);">
                        <span>Total:</span>
                        <span>${formatPrice(order.totals.total)}</span>
                    </div>
                </div>
                
                ${order.payment ? `
                <div>
                    <h4 style="font-weight: 700; margin-bottom: 0.5rem;">Método de Pago</h4>
                    <p>${order.payment.method === 'transfer' ? 'Transferencia Bancaria' : order.payment.method === 'mercadopago' ? 'MercadoPago' : 'Efectivo'}</p>
                </div>
                ` : ''}
            </div>
        `;
        
        elements.orderDetailModal.classList.add('active');
    }

    // ===== SEGURIDAD =====
    
    function handlePasswordSubmit(e) {
        e.preventDefault();
        
        const currentPassword = elements.currentPassword.value;
        const newPassword = elements.newPassword.value;
        const confirmPassword = elements.confirmNewPassword.value;
        
        // Validar contraseña actual
        if (state.user.password !== currentPassword) {
            showNotification('La contraseña actual es incorrecta', 'error');
            return;
        }
        
        // Validar nueva contraseña
        if (newPassword.length < 6) {
            showNotification('La contraseña debe tener al menos 6 caracteres', 'error');
            return;
        }
        
        if (newPassword !== confirmPassword) {
            showNotification('Las contraseñas no coinciden', 'error');
            return;
        }
        
        // Actualizar contraseña
        state.user.password = newPassword;
        
        // Actualizar en la lista de usuarios
        const users = JSON.parse(localStorage.getItem('aguardi_users') || '[]');
        const userIndex = users.findIndex(u => u.id === state.user.id);
        
        if (userIndex !== -1) {
            users[userIndex].password = newPassword;
            localStorage.setItem('aguardi_users', JSON.stringify(users));
        }
        
        // Limpiar formulario
        elements.passwordForm.reset();
        
        showNotification('Contraseña actualizada correctamente', 'success');
    }

    function deleteAccount() {
        if (!confirm('¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.')) {
            return;
        }
        
        if (!confirm('Por favor confirma nuevamente. Se eliminarán todos tus datos y pedidos.')) {
            return;
        }
        
        // Eliminar usuario de la lista
        const users = JSON.parse(localStorage.getItem('aguardi_users') || '[]');
        const filteredUsers = users.filter(u => u.id !== state.user.id);
        localStorage.setItem('aguardi_users', JSON.stringify(filteredUsers));
        
        // Cerrar sesión
        localStorage.removeItem('aguardi_user');
        
        showNotification('Cuenta eliminada correctamente', 'success');
        
        // Redirigir después de un delay
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 2000);
    }

    // ===== EVENT LISTENERS =====
    
    function initEventListeners() {
        // Tab navigation
        elements.profileNavLinks.forEach(link => {
            if (link.dataset.tab) {
                link.addEventListener('click', (e) => {
                    e.preventDefault();
                    switchTab(link.dataset.tab);
                });
            }
        });
        
        // Logout
        elements.logoutBtn.addEventListener('click', () => {
            if (window.AGUARDI && window.AGUARDI.logout) {
                window.AGUARDI.logout();
            } else {
                localStorage.removeItem('aguardi_user');
                window.location.href = 'index.html';
            }
        });
        
        // Personal info form
        elements.personalInfoForm.addEventListener('submit', handlePersonalInfoSubmit);
        elements.cancelEdit.addEventListener('click', cancelEdit);
        
        // Password form
        elements.passwordForm.addEventListener('submit', handlePasswordSubmit);
        
        // Delete account
        elements.deleteAccountBtn.addEventListener('click', deleteAccount);
        
        // Order modal
        elements.orderDetailClose.addEventListener('click', () => {
            elements.orderDetailModal.classList.remove('active');
        });
        elements.orderDetailClose2.addEventListener('click', () => {
            elements.orderDetailModal.classList.remove('active');
        });
        
        // Cerrar modal al hacer clic en backdrop
        const modalBackdrop = elements.orderDetailModal.querySelector('.modal-backdrop');
        if (modalBackdrop) {
            modalBackdrop.addEventListener('click', () => {
                elements.orderDetailModal.classList.remove('active');
            });
        }
    }

    // ===== FUNCIONES GLOBALES =====
    window.viewOrderDetail = viewOrderDetail;

    // ===== INICIALIZACIÓN =====
    
    function init() {
        loadUserData();
        loadOrders();
        initEventListeners();
        
        console.log('Perfil de usuario inicializado');
    }

    // Inicializar cuando el DOM esté listo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();