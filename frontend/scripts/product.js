// FILE: scripts/product.js
// Propósito: Lógica de la página de producto individual
// Descripción: Cargar producto, galería, cantidad, WhatsApp, productos relacionados
// Uso: Importar en product.html después de main.js

(function() {
    'use strict';

    // ===== CONFIGURACIÓN =====
    const CONFIG = {
        WHATSAPP_NUMBER: '5492235723698', // Número de WhatsApp (sin +, espacios ni guiones)
        RELATED_PRODUCTS_COUNT: 4
    };

    // ===== MOCK DATA - Productos =====
    // En producción, estos vendrían de una API
    const MOCK_PRODUCTS = [
        {
            id: 1,
            name: 'Traje Elegante Niño',
            description: 'Traje completo perfecto para bodas y eventos especiales. Incluye saco, pantalón, camisa y corbata. Confeccionado en tela de alta calidad, cómodo y elegante. Disponible en varios colores.',
            price: 12990,
            originalPrice: null,
            category: 'boy',
            images: [], // Array de URLs de imágenes
            isNew: true,
            onSale: false,
            stock: 15,
            weight: 0.8,
            sku: 'TRJ-NI-001'
        },
        {
            id: 2,
            name: 'Vestido de Fiesta Niña',
            description: 'Elegante vestido para cualquier ocasión especial. Diseño con tul y detalles bordados. Cierre con cremallera invisible. Perfecto para cumpleaños, bodas y eventos formales.',
            price: 15990,
            originalPrice: 19990,
            category: 'girl',
            images: [],
            isNew: false,
            onSale: true,
            stock: 8,
            weight: 0.5,
            sku: 'VST-NI-002'
        },
        {
            id: 3,
            name: 'Set de Accesorios Premium',
            description: 'Moños, tiradores y corbatas de alta calidad. Set completo para completar cualquier look elegante. Incluye 3 corbatas, 2 moños y un par de tiradores. Ideal para regalo.',
            price: 3990,
            originalPrice: null,
            category: 'accessories',
            images: [],
            isNew: false,
            onSale: false,
            stock: 25,
            weight: 0.2,
            sku: 'ACC-SET-003'
        },
        {
            id: 4,
            name: 'Conjunto Bebé Bautismo',
            description: 'Hermoso conjunto blanco para bautismo. 100% algodón suave. Incluye enterito, gorro y zapatitos. Detalles bordados a mano. Tallas disponibles de 0 a 12 meses.',
            price: 8990,
            originalPrice: null,
            category: 'baby',
            images: [],
            isNew: true,
            onSale: false,
            stock: 12,
            weight: 0.3,
            sku: 'BBY-BAU-004'
        },
        {
            id: 5,
            name: 'Vestido Casual Niña',
            description: 'Cómodo y elegante para el día a día. Tela fresca y suave. Diseño versátil que combina con todo. Perfecto para el colegio o paseos casuales.',
            price: 9990,
            originalPrice: 12990,
            category: 'girl',
            images: [],
            isNew: false,
            onSale: true,
            stock: 20,
            weight: 0.4,
            sku: 'VST-CAS-005'
        }
    ];

    // ===== ESTADO =====
    let state = {
        currentProduct: null,
        currentImageIndex: 0,
        quantity: 1,
        allProducts: []
    };

    // ===== ELEMENTOS DEL DOM =====
    const elements = {
        // Estados
        loadingState: document.getElementById('loadingState'),
        errorState: document.getElementById('errorState'),
        productContainer: document.getElementById('productContainer'),
        
        // Breadcrumb
        breadcrumbCategory: document.getElementById('breadcrumbCategory'),
        breadcrumbProduct: document.getElementById('breadcrumbProduct'),
        
        // Gallery
        mainImageContainer: document.getElementById('mainImageContainer'),
        mainImageIcon: document.getElementById('mainImageIcon'),
        galleryThumbnails: document.getElementById('galleryThumbnails'),
        productBadges: document.getElementById('productBadges'),
        
        // Info
        categoryBadge: document.getElementById('categoryBadge'),
        productTitle: document.getElementById('productTitle'),
        productPrice: document.getElementById('productPrice'),
        productPriceOriginal: document.getElementById('productPriceOriginal'),
        productDiscount: document.getElementById('productDiscount'),
        stockStatus: document.getElementById('stockStatus'),
        stockText: document.getElementById('stockText'),
        productDescription: document.getElementById('productDescription'),
        
        // Details
        productSku: document.getElementById('productSku'),
        productCategory: document.getElementById('productCategory'),
        productStock: document.getElementById('productStock'),
        productWeight: document.getElementById('productWeight'),
        
        // Quantity
        quantityInput: document.getElementById('quantityInput'),
        decreaseBtn: document.getElementById('decreaseBtn'),
        increaseBtn: document.getElementById('increaseBtn'),
        
        // Actions
        addToCartBtn: document.getElementById('addToCartBtn'),
        whatsappBtn: document.getElementById('whatsappBtn'),
        
        // Share
        shareFacebook: document.getElementById('shareFacebook'),
        shareTwitter: document.getElementById('shareTwitter'),
        shareCopy: document.getElementById('shareCopy'),
        
        // Related
        relatedSection: document.getElementById('relatedSection'),
        relatedProducts: document.getElementById('relatedProducts'),
        
        // Modal
        imageModal: document.getElementById('imageModal'),
        modalBackdrop: document.getElementById('modalBackdrop'),
        modalClose: document.getElementById('modalClose'),
        modalImage: document.getElementById('modalImage'),
        modalPrev: document.getElementById('modalPrev'),
        modalNext: document.getElementById('modalNext')
    };

    // ===== UTILIDADES =====

    /**
     * Obtener ID del producto desde URL
     * @returns {number|null} ID del producto o null
     */
    function getProductIdFromURL() {
        const urlParams = new URLSearchParams(window.location.search);
        const id = urlParams.get('id');
        return id ? parseInt(id) : null;
    }

    /**
     * Obtener producto por ID
     * @param {number} id - ID del producto
     * @returns {Promise<Object|null>} Producto o null
     */
    async function fetchProduct(id) {
        // Simular delay de red
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // En producción:
        // const response = await fetch(`/api/products/${id}`);
        // return await response.json();
        
        return MOCK_PRODUCTS.find(p => p.id === id) || null;
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

    // ===== CARGA DE PRODUCTO =====

    /**
     * Cargar y mostrar producto
     */
    async function loadProduct() {
        showLoading();
        
        const productId = getProductIdFromURL();
        
        if (!productId) {
            showError();
            return;
        }
        
        try {
            state.allProducts = MOCK_PRODUCTS;
            const product = await fetchProduct(productId);
            
            if (!product) {
                showError();
                return;
            }
            
            state.currentProduct = product;
            renderProduct(product);
            loadRelatedProducts(product);
            hideLoading();
        } catch (error) {
            console.error('Error al cargar producto:', error);
            showError();
        }
    }

    /**
     * Renderizar producto en la página
     * @param {Object} product - Datos del producto
     */
    function renderProduct(product) {
        // Breadcrumb
        elements.breadcrumbCategory.textContent = getCategoryName(product.category);
        elements.breadcrumbProduct.textContent = product.name;
        
        // Title & Meta
        document.title = `${product.name} - AGUARDI`;
        
        // Gallery
        renderGallery(product);
        
        // Badges
        renderBadges(product);
        
        // Category Badge
        elements.categoryBadge.textContent = getCategoryName(product.category);
        
        // Title
        elements.productTitle.textContent = product.name;
        
        // Price
        renderPrice(product);
        
        // Stock
        renderStock(product);
        
        // Description
        elements.productDescription.textContent = product.description;
        
        // Details
        elements.productSku.textContent = product.sku;
        elements.productCategory.textContent = getCategoryName(product.category);
        elements.productStock.textContent = `${product.stock} unidades`;
        elements.productWeight.textContent = `${product.weight} kg`;
        
        // Quantity limits
        elements.quantityInput.max = product.stock;
    }

    /**
     * Renderizar galería de imágenes
     * @param {Object} product - Producto
     */
    function renderGallery(product) {
        // Si no hay imágenes, mostrar placeholder
        if (!product.images || product.images.length === 0) {
            elements.mainImageIcon.textContent = getCategoryIcon(product.category);
            elements.galleryThumbnails.innerHTML = '';
            return;
        }
        
        // TODO: Implementar cuando haya imágenes reales
        // Por ahora solo mostramos el ícono
        elements.mainImageIcon.textContent = getCategoryIcon(product.category);
    }

    /**
     * Renderizar badges (NUEVO/OFERTA)
     * @param {Object} product - Producto
     */
    function renderBadges(product) {
        elements.productBadges.innerHTML = '';
        
        if (product.isNew) {
            const badge = document.createElement('span');
            badge.className = 'product-badge badge-new';
            badge.textContent = 'NUEVO';
            elements.productBadges.appendChild(badge);
        }
        
        if (product.onSale) {
            const badge = document.createElement('span');
            badge.className = 'product-badge badge-sale';
            badge.textContent = 'OFERTA';
            elements.productBadges.appendChild(badge);
        }
    }

    /**
     * Renderizar precio
     * @param {Object} product - Producto
     */
    function renderPrice(product) {
        elements.productPrice.textContent = `$${product.price.toLocaleString('es-AR')}`;
        
        if (product.originalPrice && product.originalPrice > product.price) {
            elements.productPriceOriginal.textContent = `$${product.originalPrice.toLocaleString('es-AR')}`;
            elements.productPriceOriginal.style.display = 'block';
            
            const discount = Math.round((1 - product.price / product.originalPrice) * 100);
            elements.productDiscount.textContent = `-${discount}%`;
            elements.productDiscount.style.display = 'block';
        } else {
            elements.productPriceOriginal.style.display = 'none';
            elements.productDiscount.style.display = 'none';
        }
    }

    /**
     * Renderizar estado de stock
     * @param {Object} product - Producto
     */
    function renderStock(product) {
        if (product.stock > 0) {
            elements.stockStatus.classList.remove('out-of-stock');
            elements.stockText.textContent = `En stock (${product.stock} disponibles)`;
        } else {
            elements.stockStatus.classList.add('out-of-stock');
            elements.stockText.textContent = 'Agotado';
            elements.addToCartBtn.disabled = true;
            elements.addToCartBtn.textContent = 'Producto Agotado';
        }
    }

    // ===== PRODUCTOS RELACIONADOS =====

    /**
     * Cargar productos relacionados
     * @param {Object} product - Producto actual
     */
    function loadRelatedProducts(product) {
        // Filtrar productos de la misma categoría (excluyendo el actual)
        let related = state.allProducts.filter(p => 
            p.category === product.category && p.id !== product.id
        );
        
        // Si no hay suficientes, agregar de otras categorías
        if (related.length < CONFIG.RELATED_PRODUCTS_COUNT) {
            const others = state.allProducts.filter(p => p.id !== product.id);
            related = [...related, ...others].slice(0, CONFIG.RELATED_PRODUCTS_COUNT);
        } else {
            related = related.slice(0, CONFIG.RELATED_PRODUCTS_COUNT);
        }
        
        if (related.length > 0) {
            renderRelatedProducts(related);
            elements.relatedSection.style.display = 'block';
        }
    }

    /**
     * Renderizar productos relacionados
     * @param {Array} products - Array de productos
     */
    function renderRelatedProducts(products) {
        elements.relatedProducts.innerHTML = '';
        
        products.forEach(product => {
            const card = createProductCard(product);
            elements.relatedProducts.appendChild(card);
        });
    }

    /**
     * Crear tarjeta de producto
     * @param {Object} product - Producto
     * @returns {HTMLElement} Elemento de tarjeta
     */
    function createProductCard(product) {
        const card = document.createElement('div');
        card.className = 'product-card';
        
        let badgeHTML = '';
        if (product.isNew) {
            badgeHTML = '<span class="product-badge badge-new">NUEVO</span>';
        } else if (product.onSale) {
            badgeHTML = '<span class="product-badge badge-sale">OFERTA</span>';
        }
        
        card.innerHTML = `
            <a href="product.html?id=${product.id}" class="product-link">
                <div class="product-image">
                    <div class="image-placeholder">
                        <span class="placeholder-icon">${getCategoryIcon(product.category)}</span>
                    </div>
                    ${badgeHTML}
                </div>
                <div class="product-info">
                    <h3 class="product-title">${product.name}</h3>
                    <p class="product-description">${product.description.substring(0, 60)}...</p>
                    <div class="product-footer">
                        <span class="product-price">$${product.price.toLocaleString('es-AR')}</span>
                        <button class="btn-icon" aria-label="Agregar al carrito">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="9" cy="21" r="1"></circle>
                                <circle cx="20" cy="21" r="1"></circle>
                                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                            </svg>
                        </button>
                    </div>
                </div>
            </a>
        `;
        
        return card;
    }

    // ===== CONTROLES DE CANTIDAD =====

    /**
     * Actualizar cantidad
     * @param {number} delta - Cambio en la cantidad (+1 o -1)
     */
    function updateQuantity(delta) {
        const currentValue = parseInt(elements.quantityInput.value);
        const newValue = currentValue + delta;
        const max = parseInt(elements.quantityInput.max);
        const min = parseInt(elements.quantityInput.min);
        
        if (newValue >= min && newValue <= max) {
            elements.quantityInput.value = newValue;
            state.quantity = newValue;
        }
        
        updateQuantityButtons();
    }

    /**
     * Actualizar estado de botones de cantidad
     */
    function updateQuantityButtons() {
        const value = parseInt(elements.quantityInput.value);
        const max = parseInt(elements.quantityInput.max);
        const min = parseInt(elements.quantityInput.min);
        
        elements.decreaseBtn.disabled = value <= min;
        elements.increaseBtn.disabled = value >= max;
    }

    // ===== ACCIONES =====

    /**
     * Agregar al carrito
     */
    function handleAddToCart() {
        if (!state.currentProduct) return;
        
        const quantity = parseInt(elements.quantityInput.value);
        
        if (window.AGUARDI && window.AGUARDI.addToCart) {
            window.AGUARDI.addToCart(state.currentProduct, quantity);
        } else {
            console.error('AGUARDI global no encontrado');
        }
    }

    /**
     * Abrir WhatsApp con mensaje prellenado
     */
    function handleWhatsApp() {
        if (!state.currentProduct) return;
        
        const quantity = parseInt(elements.quantityInput.value);
        const message = `Hola! Me interesa el producto:
*${state.currentProduct.name}*
Cantidad: ${quantity}
Precio: $${state.currentProduct.price.toLocaleString('es-AR')}
Imagen: ${state.currentProduct.images && state.currentProduct.images.length > 0 ? state.currentProduct.images[0] : 'No disponible'}

Link: ${window.location.href}`;
        
        const encodedMessage = encodeURIComponent(message);
        const whatsappURL = `https://wa.me/${CONFIG.WHATSAPP_NUMBER}?text=${encodedMessage}`;
        
        window.open(whatsappURL, '_blank');
    }

    /**
     * Compartir en Facebook
     */
    function shareOnFacebook() {
        const url = encodeURIComponent(window.location.href);
        window.open(`https://www.facebook.com/sharer/sharer.php?u=${url}`, '_blank');
    }

    /**
     * Compartir en Twitter
     */
    function shareOnTwitter() {
        const text = encodeURIComponent(`${state.currentProduct.name} - AGUARDI`);
        const url = encodeURIComponent(window.location.href);
        window.open(`https://twitter.com/intent/tweet?text=${text}&url=${url}`, '_blank');
    }

    /**
     * Copiar enlace al portapapeles
     */
    async function copyLink() {
        try {
            await navigator.clipboard.writeText(window.location.href);
            if (window.AGUARDI && window.AGUARDI.showNotification) {
                window.AGUARDI.showNotification('Enlace copiado al portapapeles', 'success');
            }
        } catch (error) {
            console.error('Error al copiar enlace:', error);
        }
    }

    // ===== MODAL DE IMAGEN =====

    /**
     * Abrir modal de imagen
     */
    function openImageModal() {
        elements.imageModal.classList.add('active');
        document.body.style.overflow = 'hidden';
        
        // Copiar el ícono actual al modal
        const currentIcon = elements.mainImageIcon.textContent;
        const modalIcon = elements.modalImage.querySelector('.placeholder-icon');
        if (modalIcon) {
            modalIcon.textContent = currentIcon;
        }
    }

    /**
     * Cerrar modal de imagen
     */
    function closeImageModal() {
        elements.imageModal.classList.remove('active');
        document.body.style.overflow = '';
    }

    // ===== ESTADOS DE CARGA =====

    /**
     * Mostrar estado de carga
     */
    function showLoading() {
        elements.loadingState.style.display = 'flex';
        elements.errorState.style.display = 'none';
        elements.productContainer.style.display = 'none';
    }

    /**
     * Ocultar estado de carga
     */
    function hideLoading() {
        elements.loadingState.style.display = 'none';
        elements.productContainer.style.display = 'grid';
    }

    /**
     * Mostrar error
     */
    function showError() {
        elements.loadingState.style.display = 'none';
        elements.errorState.style.display = 'flex';
        elements.productContainer.style.display = 'none';
    }

    // ===== EVENT LISTENERS =====

    /**
     * Inicializar event listeners
     */
    function initEventListeners() {
        // Quantity controls
        elements.decreaseBtn.addEventListener('click', () => updateQuantity(-1));
        elements.increaseBtn.addEventListener('click', () => updateQuantity(1));
        
        elements.quantityInput.addEventListener('change', (e) => {
            let value = parseInt(e.target.value);
            const max = parseInt(e.target.max);
            const min = parseInt(e.target.min);
            
            if (isNaN(value) || value < min) value = min;
            if (value > max) value = max;
            
            e.target.value = value;
            state.quantity = value;
            updateQuantityButtons();
        });
        
        // Actions
        elements.addToCartBtn.addEventListener('click', handleAddToCart);
        elements.whatsappBtn.addEventListener('click', handleWhatsApp);
        
        // Share
        elements.shareFacebook.addEventListener('click', shareOnFacebook);
        elements.shareTwitter.addEventListener('click', shareOnTwitter);
        elements.shareCopy.addEventListener('click', copyLink);
        
        // Image modal
        elements.mainImageContainer.addEventListener('click', openImageModal);
        elements.modalClose.addEventListener('click', closeImageModal);
        elements.modalBackdrop.addEventListener('click', closeImageModal);
        
        // Cerrar modal con ESC
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && elements.imageModal.classList.contains('active')) {
                closeImageModal();
            }
        });
        
        // Navigation en modal (por ahora disabled ya que no hay múltiples imágenes)
        elements.modalPrev.disabled = true;
        elements.modalNext.disabled = true;
    }

    // ===== INICIALIZACIÓN =====

    /**
     * Inicializar página de producto
     */
    function init() {
        initEventListeners();
        loadProduct();
        updateQuantityButtons();
        
        console.log('Página de producto AGUARDI inicializada');
    }

    // Inicializar cuando el DOM esté listo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();