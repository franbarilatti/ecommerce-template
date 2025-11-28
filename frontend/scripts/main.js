// FILE: scripts/main.js
// Propósito: JavaScript principal para AGUARDI
// Descripción: Manejo de navegación, carrito y funcionalidades básicas
// Uso: Importar en index.html y otras páginas principales

(function() {
    'use strict';

    // ===== CONSTANTES Y CONFIGURACIÓN =====
    const STORAGE_KEYS = {
        CART: 'aguardi_cart',
        USER: 'aguardi_user',
        PRODUCTS: 'aguardi_products'
    };

    // ===== UTILIDADES =====
    
    /**
     * Obtener datos del localStorage de forma segura
     * @param {string} key - Clave del localStorage
     * @param {*} defaultValue - Valor por defecto si no existe
     * @returns {*} Datos parseados o valor por defecto
     */
    function getStorage(key, defaultValue = null) {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : defaultValue;
        } catch (error) {
            console.error('Error al leer localStorage:', error);
            return defaultValue;
        }
    }

    /**
     * Guardar datos en localStorage de forma segura
     * @param {string} key - Clave del localStorage
     * @param {*} value - Valor a guardar
     * @returns {boolean} True si se guardó correctamente
     */
    function setStorage(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
            return true;
        } catch (error) {
            console.error('Error al guardar en localStorage:', error);
            return false;
        }
    }

    // ===== GESTIÓN DE NAVEGACIÓN =====
    
    /**
     * Inicializar menú móvil
     */
    function initMobileMenu() {
        const navToggle = document.getElementById('navToggle');
        const navMobile = document.getElementById('navMobile');
        
        if (!navToggle || !navMobile) return;

        navToggle.addEventListener('click', function() {
            const isOpen = navMobile.classList.toggle('active');
            navToggle.setAttribute('aria-expanded', isOpen);
            
            // Prevenir scroll cuando el menú está abierto
            document.body.style.overflow = isOpen ? 'hidden' : '';
        });

        // Cerrar menú al hacer clic en un enlace
        const mobileLinks = navMobile.querySelectorAll('.nav-link-mobile');
        mobileLinks.forEach(link => {
            link.addEventListener('click', () => {
                navMobile.classList.remove('active');
                navToggle.setAttribute('aria-expanded', 'false');
                document.body.style.overflow = '';
            });
        });

        // Cerrar menú al hacer clic fuera
        document.addEventListener('click', function(e) {
            if (!navToggle.contains(e.target) && !navMobile.contains(e.target)) {
                navMobile.classList.remove('active');
                navToggle.setAttribute('aria-expanded', 'false');
                document.body.style.overflow = '';
            }
        });
    }

    // ===== GESTIÓN DEL CARRITO =====
    
    /**
     * Obtener carrito actual
     * @returns {Array} Array de productos en el carrito
     */
    function getCart() {
        return getStorage(STORAGE_KEYS.CART, []);
    }

    /**
     * Guardar carrito
     * @param {Array} cart - Array de productos
     */
    function saveCart(cart) {
        setStorage(STORAGE_KEYS.CART, cart);
        updateCartCount();
    }

    /**
     * Agregar producto al carrito
     * @param {Object} product - Producto a agregar
     * @param {number} quantity - Cantidad
     */
    function addToCart(product, quantity = 1) {
        const cart = getCart();
        const existingItem = cart.find(item => item.id === product.id);

        if (existingItem) {
            existingItem.quantity += quantity;
        } else {
            cart.push({
                ...product,
                quantity: quantity
            });
        }

        saveCart(cart);
        showNotification('Producto agregado al carrito', 'success');
    }

    /**
     * Actualizar contador del carrito en el navbar
     */
    function updateCartCount() {
        const cartCountElement = document.getElementById('cartCount');
        if (!cartCountElement) return;

        const cart = getCart();
        const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
        cartCountElement.textContent = totalItems;
        
        // Mostrar/ocultar badge según cantidad
        cartCountElement.style.display = totalItems > 0 ? 'flex' : 'none';
    }

    /**
     * Mostrar notificación temporal
     * @param {string} message - Mensaje a mostrar
     * @param {string} type - Tipo de notificación (success, error, warning, info)
     */
    function showNotification(message, type = 'info') {
        // Crear elemento de notificación
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.textContent = message;
        
        // Estilos inline (se pueden mover a CSS)
        notification.style.cssText = `
            position: fixed;
            top: 5rem;
            right: 1rem;
            padding: 1rem 1.5rem;
            background-color: ${type === 'success' ? '#10B981' : '#3B82F6'};
            color: white;
            border-radius: 0.5rem;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
            z-index: 9999;
            animation: slideIn 0.3s ease-out;
            max-width: 300px;
        `;

        document.body.appendChild(notification);

        // Eliminar después de 3 segundos
        setTimeout(() => {
            notification.style.animation = 'slideOut 0.3s ease-out';
            setTimeout(() => {
                notification.remove();
            }, 300);
        }, 3000);
    }

    // ===== GESTIÓN DE USUARIO =====
    
    /**
     * Obtener usuario actual
     * @returns {Object|null} Usuario o null si no está logueado
     */
    function getCurrentUser() {
        return getStorage(STORAGE_KEYS.USER, null);
    }

    /**
     * Verificar si el usuario está logueado
     * @returns {boolean} True si está logueado
     */
    function isLoggedIn() {
        const user = getCurrentUser();
        return user !== null && user.logged === true;
    }

    /**
     * Actualizar UI según estado de autenticación
     */
    function updateAuthUI() {
        const user = getCurrentUser();
        const loginBtn = document.querySelector('.nav-btn');
        
        if (!loginBtn) return;

        if (user && user.logged) {
            // Usuario logueado - mostrar menú de perfil
            loginBtn.textContent = user.fullName ? user.fullName.split(' ')[0] : 'Mi Cuenta';
            loginBtn.href = '#';
            loginBtn.onclick = (e) => {
                e.preventDefault();
                toggleUserMenu();
            };
            
            // Crear menú desplegable si no existe
            createUserMenu();
        } else {
            // Usuario no logueado
            loginBtn.textContent = 'Iniciar Sesión';
            loginBtn.href = 'login.html';
            loginBtn.onclick = null;
            
            // Remover menú si existe
            const existingMenu = document.getElementById('userMenu');
            if (existingMenu) {
                existingMenu.remove();
            }
        }
    }
    
    /**
     * Crear menú desplegable de usuario
     */
    function createUserMenu() {
        // Verificar si ya existe
        if (document.getElementById('userMenu')) return;
        
        const user = getCurrentUser();
        if (!user) return;
        
        const menu = document.createElement('div');
        menu.id = 'userMenu';
        menu.className = 'user-menu';
        menu.style.cssText = `
            position: absolute;
            top: 100%;
            right: 0;
            margin-top: 0.5rem;
            background-color: white;
            border-radius: 0.75rem;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
            padding: 0.5rem;
            min-width: 200px;
            display: none;
            z-index: 1000;
        `;
        
        menu.innerHTML = `
            <div style="padding: 0.75rem 1rem; border-bottom: 1px solid #E5E7EB;">
                <div style="font-weight: 600; color: #1F2937;">${user.fullName}</div>
                <div style="font-size: 0.875rem; color: #6B7280;">${user.email}</div>
            </div>
            <a href="#" style="display: block; padding: 0.75rem 1rem; color: #374151; text-decoration: none; transition: background-color 0.2s;" onmouseover="this.style.backgroundColor='#F3F4F6'" onmouseout="this.style.backgroundColor='transparent'">
                👤 Mi Perfil
            </a>
            <a href="#" style="display: block; padding: 0.75rem 1rem; color: #374151; text-decoration: none; transition: background-color 0.2s;" onmouseover="this.style.backgroundColor='#F3F4F6'" onmouseout="this.style.backgroundColor='transparent'">
                📦 Mis Pedidos
            </a>
            ${user.role === 'admin' ? '<a href="admin/index.html" style="display: block; padding: 0.75rem 1rem; color: #374151; text-decoration: none; transition: background-color 0.2s;" onmouseover="this.style.backgroundColor=\'#F3F4F6\'" onmouseout="this.style.backgroundColor=\'transparent\'">⚙️ Admin Panel</a>' : ''}
            <div style="border-top: 1px solid #E5E7EB; margin-top: 0.5rem; padding-top: 0.5rem;">
                <button id="logoutBtn" style="display: block; width: 100%; padding: 0.75rem 1rem; color: #EF4444; text-align: left; background: none; border: none; cursor: pointer; transition: background-color 0.2s; border-radius: 0.5rem;" onmouseover="this.style.backgroundColor='#FEE2E2'" onmouseout="this.style.backgroundColor='transparent'">
                    🚪 Cerrar Sesión
                </button>
            </div>
        `;
        
        const navActions = document.querySelector('.nav-actions');
        if (navActions) {
            navActions.style.position = 'relative';
            navActions.appendChild(menu);
            
            // Event listener para cerrar sesión
            const logoutBtn = menu.querySelector('#logoutBtn');
            if (logoutBtn) {
                logoutBtn.addEventListener('click', () => {
                    if (window.AGUARDI && window.AGUARDI.logout) {
                        window.AGUARDI.logout();
                    } else {
                        localStorage.removeItem('aguardi_user');
                        window.location.href = 'index.html';
                    }
                });
            }
        }
        
        // Cerrar menú al hacer clic fuera
        document.addEventListener('click', (e) => {
            if (!navActions.contains(e.target)) {
                menu.style.display = 'none';
            }
        });
    }
    
    /**
     * Toggle menú de usuario
     */
    function toggleUserMenu() {
        const menu = document.getElementById('userMenu');
        if (menu) {
            menu.style.display = menu.style.display === 'none' ? 'block' : 'none';
        }
    }

    // ===== PRODUCTOS =====
    
    /**
     * Cargar productos destacados (mock)
     * En producción, esto vendría de una API
     */
    function loadFeaturedProducts() {
        const productsContainer = document.getElementById('featuredProducts');
        if (!productsContainer) return;

        // Productos de ejemplo (en producción vendrían de data/products.json o API)
        const featuredProducts = [
            {
                id: 1,
                name: 'Traje Elegante Niño',
                description: 'Perfecto para bodas y eventos especiales',
                price: 12990,
                image: null,
                category: 'nino',
                isNew: true,
                onSale: false
            },
            {
                id: 2,
                name: 'Vestido de Fiesta Niña',
                description: 'Elegante y cómodo para cualquier ocasión',
                price: 15990,
                image: null,
                category: 'nina',
                isNew: false,
                onSale: true
            },
            {
                id: 3,
                name: 'Set de Accesorios',
                description: 'Moños, tiradores y corbatas',
                price: 3990,
                image: null,
                category: 'accesorios',
                isNew: false,
                onSale: false
            }
        ];

        // Limpiar contenedor (mantener solo si está vacío)
        if (productsContainer.children.length <= 1) {
            productsContainer.innerHTML = '';
            
            featuredProducts.forEach(product => {
                const productCard = createProductCard(product);
                productsContainer.appendChild(productCard);
            });
        }
    }

    /**
     * Crear tarjeta de producto
     * @param {Object} product - Datos del producto
     * @returns {HTMLElement} Elemento DOM de la tarjeta
     */
    function createProductCard(product) {
        const card = document.createElement('div');
        card.className = 'product-card';

        // Determinar ícono según categoría
        const icons = {
            'bebe': '👶',
            'nino': '👔',
            'nina': '👗',
            'fiesta': '🎉',
            'accesorios': '🎀'
        };
        const icon = icons[product.category] || '👔';

        // Crear badge si aplica
        let badgeHTML = '';
        if (product.isNew) {
            badgeHTML = '<span class="product-badge badge-new">NUEVO</span>';
        } else if (product.onSale) {
            badgeHTML = '<span class="product-badge badge-sale">OFERTA</span>';
        }

        card.innerHTML = `
            <div class="product-image">
                <div class="image-placeholder">
                    <span class="placeholder-icon">${icon}</span>
                </div>
                ${badgeHTML}
            </div>
            <div class="product-info">
                <h3 class="product-title">${product.name}</h3>
                <p class="product-description">${product.description}</p>
                <div class="product-footer">
                    <span class="product-price">$${product.price.toLocaleString('es-AR')}</span>
                    <button class="btn-icon" aria-label="Agregar al carrito" data-product-id="${product.id}">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <circle cx="9" cy="21" r="1"></circle>
                            <circle cx="20" cy="21" r="1"></circle>
                            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                        </svg>
                    </button>
                </div>
            </div>
        `;

        // Agregar event listener al botón de carrito
        const addToCartBtn = card.querySelector('.btn-icon');
        addToCartBtn.addEventListener('click', (e) => {
            e.preventDefault();
            addToCart(product, 1);
        });

        return card;
    }

    // ===== SMOOTH SCROLL =====
    
    /**
     * Implementar smooth scroll para enlaces internos
     */
    function initSmoothScroll() {
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function(e) {
                const href = this.getAttribute('href');
                if (href === '#') return;
                
                const target = document.querySelector(href);
                if (target) {
                    e.preventDefault();
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            });
        });
    }

    // ===== ANIMACIONES CSS =====
    
    /**
     * Agregar estilos de animación para notificaciones
     */
    function addAnimationStyles() {
        if (document.getElementById('notification-styles')) return;

        const style = document.createElement('style');
        style.id = 'notification-styles';
        style.textContent = `
            @keyframes slideIn {
                from {
                    transform: translateX(400px);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
            
            @keyframes slideOut {
                from {
                    transform: translateX(0);
                    opacity: 1;
                }
                to {
                    transform: translateX(400px);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(style);
    }

    // ===== INICIALIZACIÓN =====
    
    /**
     * Inicializar todas las funcionalidades
     */
    function init() {
        // Agregar estilos de animación
        addAnimationStyles();
        
        // Inicializar menú móvil
        initMobileMenu();
        
        // Actualizar contador del carrito
        updateCartCount();
        
        // Actualizar UI de autenticación
        updateAuthUI();
        
        // Cargar productos destacados
        loadFeaturedProducts();
        
        // Inicializar smooth scroll
        initSmoothScroll();
        
        console.log('AGUARDI - Sistema inicializado correctamente');
    }

    // ===== EXPORTAR FUNCIONES GLOBALES =====
    // Para uso desde otros scripts
    window.AGUARDI = {
        getCart,
        addToCart,
        saveCart,
        getCurrentUser,
        isLoggedIn,
        showNotification,
        getStorage,
        setStorage,
        STORAGE_KEYS
    };

    // Inicializar cuando el DOM esté listo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();