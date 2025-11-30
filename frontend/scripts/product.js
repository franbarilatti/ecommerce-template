// FILE: scripts/product.js
// Propósito: Lógica de la página de producto individual
// Descripción: Cargar producto, galería, cantidad, WhatsApp, productos relacionados
// Uso: Importar en product.html después de main.js

// ============================================
// FILE: frontend/scripts/product.js (ACTUALIZADO)
// Propósito: Página de detalle de producto conectado con el backend
// ============================================

let currentProduct = null;
let currentImageIndex = 0;
let selectedQuantity = 1;

// ========================================
// CARGAR PRODUCTO
// ========================================

async function loadProduct() {
    try {
        // Obtener ID del producto desde URL
        const urlParams = new URLSearchParams(window.location.search);
        const productId = urlParams.get('id');

        if (!productId) {
            showNotification('Producto no encontrado', 'error');
            setTimeout(() => {
                window.location.href = '/catalog.html';
            }, 2000);
            return;
        }

        // Mostrar loading
        showLoading();

        // Obtener producto desde la API
        currentProduct = await API.products.getProductById(productId);

        // Renderizar producto
        renderProduct();

        // Cargar productos relacionados
        loadRelatedProducts(currentProduct.categorySlug);

    } catch (error) {
        console.error('Error cargando producto:', error);
        
        hideLoading();
        
        if (error.status === 404) {
            showNotification('Producto no encontrado', 'error');
        } else {
            showNotification('Error al cargar el producto', 'error');
        }

        setTimeout(() => {
            window.location.href = '/catalog.html';
        }, 2000);
    }
}

// ========================================
// RENDERIZAR PRODUCTO
// ========================================

function renderProduct() {
    hideLoading();

    const product = currentProduct;

    // Título de la página
    document.title = `${product.name} - AGUARDI`;

    // Breadcrumb
    const breadcrumbEl = document.getElementById('breadcrumb');
    if (breadcrumbEl) {
        breadcrumbEl.innerHTML = `
            <a href="/index.html">Inicio</a> /
            <a href="/catalog.html">Catálogo</a> /
            ${product.categoryName ? `<a href="/catalog.html?category=${product.categorySlug}">${product.categoryName}</a> /` : ''}
            <span>${product.name}</span>
        `;
    }

    // Galería de imágenes
    renderImageGallery();

    // Información del producto
    renderProductInfo();

    // Descripción
    renderProductDescription();
}

// ========================================
// GALERÍA DE IMÁGENES
// ========================================

function renderImageGallery() {
    const product = currentProduct;
    const mainImageEl = document.getElementById('mainImage');
    const thumbnailsEl = document.getElementById('thumbnails');

    // Preparar array de imágenes
    const images = product.images && product.images.length > 0 
        ? product.images 
        : [{ url: product.mainImageUrl || '/images/placeholder.jpg', altText: product.name }];

    // Imagen principal
    if (mainImageEl) {
        mainImageEl.innerHTML = `
            <img src="${images[currentImageIndex].url}" 
                 alt="${images[currentImageIndex].altText || product.name}"
                 onerror="this.src='/images/placeholder.jpg'"
                 id="productMainImage">
            ${product.onSale ? '<span class="badge badge-sale">¡OFERTA!</span>' : ''}
            ${product.isNew ? '<span class="badge badge-new">NUEVO</span>' : ''}
            ${product.stock === 0 ? '<span class="badge badge-sold-out">AGOTADO</span>' : ''}
        `;

        // Click en imagen para zoom (opcional)
        const imgEl = document.getElementById('productMainImage');
        if (imgEl) {
            imgEl.addEventListener('click', () => openImageModal(images[currentImageIndex].url));
        }
    }

    // Miniaturas
    if (thumbnailsEl && images.length > 1) {
        thumbnailsEl.innerHTML = images.map((img, index) => `
            <img src="${img.url}" 
                 alt="${img.altText || product.name}"
                 class="thumbnail ${index === currentImageIndex ? 'active' : ''}"
                 onclick="changeMainImage(${index})"
                 onerror="this.src='/images/placeholder.jpg'">
        `).join('');
    }
}

function changeMainImage(index) {
    currentImageIndex = index;
    renderImageGallery();
}

function openImageModal(imageUrl) {
    // Crear modal para zoom de imagen
    const modal = document.createElement('div');
    modal.className = 'image-modal';
    modal.innerHTML = `
        <div class="modal-content">
            <span class="close" onclick="this.parentElement.parentElement.remove()">&times;</span>
            <img src="${imageUrl}" alt="Producto">
        </div>
    `;
    modal.addEventListener('click', (e) => {
        if (e.target === modal) modal.remove();
    });
    document.body.appendChild(modal);
}

// ========================================
// INFORMACIÓN DEL PRODUCTO
// ========================================

function renderProductInfo() {
    const product = currentProduct;
    const productInfoEl = document.getElementById('productInfo');

    if (!productInfoEl) return;

    // Calcular descuento
    const discount = product.onSale && product.salePrice 
        ? Math.round((1 - product.salePrice / product.price) * 100)
        : 0;

    productInfoEl.innerHTML = `
        <h1>${product.name}</h1>
        
        ${product.categoryName ? `
            <p class="product-category">
                <a href="/catalog.html?category=${product.categorySlug}">${product.categoryName}</a>
            </p>
        ` : ''}

        <div class="product-price-section">
            ${product.onSale && product.salePrice ? `
                <span class="price-old">$${product.price.toFixed(2)}</span>
                <span class="price-current">$${product.salePrice.toFixed(2)}</span>
                <span class="price-discount">-${discount}% OFF</span>
            ` : `
                <span class="price-current">$${product.price.toFixed(2)}</span>
            `}
        </div>

        <div class="product-stock">
            ${product.stock > 0 ? `
                <span class="in-stock">
                    <i class="fas fa-check-circle"></i>
                    ${product.stock <= 5 
                        ? `¡Solo quedan ${product.stock} unidades!` 
                        : 'Disponible en stock'}
                </span>
            ` : `
                <span class="out-of-stock">
                    <i class="fas fa-times-circle"></i>
                    Producto agotado
                </span>
            `}
        </div>

        ${product.stock > 0 ? `
            <div class="quantity-selector">
                <label>Cantidad:</label>
                <div class="quantity-controls">
                    <button onclick="changeQuantity(-1)" class="btn-quantity">
                        <i class="fas fa-minus"></i>
                    </button>
                    <input type="number" 
                           id="quantityInput" 
                           value="${selectedQuantity}" 
                           min="1" 
                           max="${product.stock}"
                           onchange="updateQuantity(this.value)">
                    <button onclick="changeQuantity(1)" class="btn-quantity">
                        <i class="fas fa-plus"></i>
                    </button>
                </div>
            </div>

            <div class="product-actions">
                <button class="btn btn-primary btn-large" onclick="addProductToCart()">
                    <i class="fas fa-shopping-cart"></i>
                    Agregar al Carrito
                </button>
                <button class="btn btn-secondary btn-large" onclick="buyNow()">
                    Comprar Ahora
                </button>
            </div>
        ` : `
            <div class="product-actions">
                <button class="btn btn-secondary btn-large" onclick="notifyWhenAvailable()">
                    <i class="fas fa-bell"></i>
                    Notificarme cuando esté disponible
                </button>
            </div>
        `}

        <div class="product-features">
            <div class="feature">
                <i class="fas fa-truck"></i>
                <span>Envío a todo el país</span>
            </div>
            <div class="feature">
                <i class="fas fa-credit-card"></i>
                <span>Múltiples medios de pago</span>
            </div>
            <div class="feature">
                <i class="fas fa-shield-alt"></i>
                <span>Compra protegida</span>
            </div>
        </div>
    `;
}

// ========================================
// DESCRIPCIÓN
// ========================================

function renderProductDescription() {
    const product = currentProduct;
    const descriptionEl = document.getElementById('productDescription');

    if (!descriptionEl) return;

    descriptionEl.innerHTML = `
        <div class="tabs">
            <button class="tab-btn active" onclick="switchTab('description')">Descripción</button>
            <button class="tab-btn" onclick="switchTab('specs')">Especificaciones</button>
            <button class="tab-btn" onclick="switchTab('shipping')">Envío</button>
        </div>

        <div class="tab-content active" id="tab-description">
            <p>${product.description || 'Sin descripción disponible.'}</p>
        </div>

        <div class="tab-content" id="tab-specs">
            <ul>
                <li><strong>SKU:</strong> ${product.sku || 'N/A'}</li>
                <li><strong>Categoría:</strong> ${product.categoryName || 'Sin categoría'}</li>
                <li><strong>Stock:</strong> ${product.stock} unidades</li>
                ${product.weight ? `<li><strong>Peso:</strong> ${product.weight}kg</li>` : ''}
            </ul>
        </div>

        <div class="tab-content" id="tab-shipping">
            <p>Realizamos envíos a todo el país a través de Correo Argentino y transportes privados.</p>
            <ul>
                <li>Envío gratis en compras mayores a $50.000</li>
                <li>Tiempo de entrega: 3-7 días hábiles</li>
                <li>Podés hacer seguimiento con tu número de tracking</li>
            </ul>
        </div>
    `;
}

function switchTab(tabName) {
    // Remover active de todos los tabs
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

    // Activar tab seleccionado
    event.target.classList.add('active');
    document.getElementById(`tab-${tabName}`).classList.add('active');
}

// ========================================
// CANTIDAD
// ========================================

function changeQuantity(delta) {
    const newQuantity = selectedQuantity + delta;
    updateQuantity(newQuantity);
}

function updateQuantity(value) {
    let quantity = parseInt(value);

    if (isNaN(quantity) || quantity < 1) {
        quantity = 1;
    }

    if (quantity > currentProduct.stock) {
        quantity = currentProduct.stock;
        showNotification(`Máximo disponible: ${currentProduct.stock}`, 'warning');
    }

    selectedQuantity = quantity;

    const quantityInput = document.getElementById('quantityInput');
    if (quantityInput) {
        quantityInput.value = selectedQuantity;
    }
}

// ========================================
// ACCIONES
// ========================================

function addProductToCart() {
    if (!currentProduct) return;

    // Agregar al carrito
    API.cart.addItem(currentProduct, selectedQuantity);

    // Mostrar notificación
    showNotification(
        `${selectedQuantity} ${selectedQuantity === 1 ? 'unidad agregada' : 'unidades agregadas'} al carrito`,
        'success'
    );

    // Animación del botón
    const btn = event.target;
    btn.classList.add('btn-success');
    btn.innerHTML = '<i class="fas fa-check"></i> Agregado';

    setTimeout(() => {
        btn.classList.remove('btn-success');
        btn.innerHTML = '<i class="fas fa-shopping-cart"></i> Agregar al Carrito';
    }, 2000);
}

function buyNow() {
    if (!currentProduct) return;

    // Agregar al carrito
    API.cart.addItem(currentProduct, selectedQuantity);

    // Redirigir a checkout
    window.location.href = '/checkout.html';
}

function notifyWhenAvailable() {
    // TODO: Implementar notificación cuando vuelva stock
    showNotification('Te notificaremos cuando el producto esté disponible', 'info');
}

// ========================================
// PRODUCTOS RELACIONADOS
// ========================================

async function loadRelatedProducts(categorySlug) {
    if (!categorySlug) return;

    try {
        const response = await API.products.getProducts({
            category: categorySlug,
            page: 0,
            size: 4
        });

        const relatedEl = document.getElementById('relatedProducts');
        if (!relatedEl || !response.content || response.content.length <= 1) return;

        // Filtrar el producto actual
        const related = response.content.filter(p => p.id !== currentProduct.id);

        if (related.length === 0) return;

        relatedEl.innerHTML = `
            <h2>Productos Relacionados</h2>
            <div class="related-grid">
                ${related.map(product => `
                    <div class="product-card-small" onclick="window.location.href='/product.html?id=${product.id}'">
                        <img src="${product.mainImageUrl || '/images/placeholder.jpg'}" 
                             alt="${product.name}"
                             onerror="this.src='/images/placeholder.jpg'">
                        <h4>${product.name}</h4>
                        <p class="price">$${(product.salePrice || product.price).toFixed(2)}</p>
                    </div>
                `).join('')}
            </div>
        `;

    } catch (error) {
        console.error('Error cargando productos relacionados:', error);
    }
}

// ========================================
// HELPERS
// ========================================

function showLoading() {
    const loadingEl = document.getElementById('loading');
    if (loadingEl) loadingEl.style.display = 'block';
}

function hideLoading() {
    const loadingEl = document.getElementById('loading');
    if (loadingEl) loadingEl.style.display = 'none';
}

// ========================================
// INICIALIZACIÓN
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    loadProduct();
});

// Hacer funciones disponibles globalmente
window.changeMainImage = changeMainImage;
window.changeQuantity = changeQuantity;
window.updateQuantity = updateQuantity;
window.addProductToCart = addProductToCart;
window.buyNow = buyNow;
window.notifyWhenAvailable = notifyWhenAvailable;
window.switchTab = switchTab;