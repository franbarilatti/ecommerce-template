// FILE: scripts/catalog.js
// Propósito: Lógica del catálogo con filtros, búsqueda y paginación
// Descripción: Manejo de productos, filtrado dinámico y gestión de estado
// Uso: Importar en catalog.html después de main.js


// ============================================
// FILE: frontend/scripts/catalog.js (ACTUALIZADO)
// Propósito: Catálogo de productos conectado con el backend
// ============================================

let currentPage = 0;
const pageSize = 12;
let currentFilters = {
    category: null,
    minPrice: null,
    maxPrice: null,
    search: null,
    sortBy: 'createdAt',
    sortDir: 'DESC'
};

// ========================================
// CARGAR PRODUCTOS
// ========================================

async function loadProducts() {
    const productsGrid = document.getElementById('productsGrid');
    const loadingEl = document.getElementById('loading');
    const noResultsEl = document.getElementById('noResults');

    try {
        // Mostrar loading
        if (loadingEl) loadingEl.style.display = 'block';
        if (noResultsEl) noResultsEl.style.display = 'none';
        if (productsGrid) productsGrid.innerHTML = '';

        // Construir parámetros
        const params = {
            page: currentPage,
            size: pageSize,
            sortBy: currentFilters.sortBy,
            sortDir: currentFilters.sortDir
        };

        // Agregar filtros opcionales
        if (currentFilters.category) params.category = currentFilters.category;
        if (currentFilters.minPrice) params.minPrice = currentFilters.minPrice;
        if (currentFilters.maxPrice) params.maxPrice = currentFilters.maxPrice;
        if (currentFilters.search) params.search = currentFilters.search;

        // Llamar a la API
        const response = await API.products.getProducts(params);

        // Ocultar loading
        if (loadingEl) loadingEl.style.display = 'none';

        // Verificar si hay productos
        if (!response.content || response.content.length === 0) {
            if (noResultsEl) noResultsEl.style.display = 'block';
            return;
        }

        // Renderizar productos
        renderProducts(response.content);

        // Renderizar paginación
        renderPagination(response);

    } catch (error) {
        console.error('Error cargando productos:', error);
        if (loadingEl) loadingEl.style.display = 'none';
        showNotification('Error al cargar productos', 'error');
    }
}

// ========================================
// RENDERIZAR PRODUCTOS
// ========================================

function renderProducts(products) {
    const productsGrid = document.getElementById('productsGrid');
    if (!productsGrid) return;

    productsGrid.innerHTML = products.map(product => `
        <div class="product-card" data-product-id="${product.id}">
            <div class="product-image">
                <img src="${product.mainImageUrl || '/images/placeholder.jpg'}" 
                     alt="${product.name}"
                     onerror="this.src='/images/placeholder.jpg'">
                ${product.onSale ? '<span class="badge badge-sale">¡OFERTA!</span>' : ''}
                ${product.isNew ? '<span class="badge badge-new">NUEVO</span>' : ''}
                ${product.stock === 0 ? '<span class="badge badge-sold-out">AGOTADO</span>' : ''}
            </div>
            <div class="product-info">
                <h3 class="product-name">${product.name}</h3>
                <p class="product-category">${product.categoryName || 'Sin categoría'}</p>
                <div class="product-price">
                    ${product.onSale && product.salePrice ? `
                        <span class="price-old">$${product.price.toFixed(2)}</span>
                        <span class="price-current">$${product.salePrice.toFixed(2)}</span>
                        <span class="price-discount">-${Math.round((1 - product.salePrice/product.price) * 100)}%</span>
                    ` : `
                        <span class="price-current">$${product.price.toFixed(2)}</span>
                    `}
                </div>
                <div class="product-actions">
                    <button class="btn btn-primary" 
                            onclick="viewProduct(${product.id})"
                            ${product.stock === 0 ? 'disabled' : ''}>
                        Ver Detalles
                    </button>
                    <button class="btn btn-secondary" 
                            onclick="addToCart(${product.id})"
                            ${product.stock === 0 ? 'disabled' : ''}>
                        <i class="fas fa-shopping-cart"></i>
                        ${product.stock === 0 ? 'Agotado' : 'Agregar'}
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

// ========================================
// PAGINACIÓN
// ========================================

function renderPagination(response) {
    const paginationEl = document.getElementById('pagination');
    if (!paginationEl) return;

    const { pageNumber, totalPages, first, last } = response;

    if (totalPages <= 1) {
        paginationEl.style.display = 'none';
        return;
    }

    paginationEl.style.display = 'flex';

    let paginationHTML = `
        <button class="btn-page" 
                onclick="changePage(${pageNumber - 1})" 
                ${first ? 'disabled' : ''}>
            <i class="fas fa-chevron-left"></i> Anterior
        </button>
    `;

    // Páginas
    const maxPages = 5;
    let startPage = Math.max(0, pageNumber - Math.floor(maxPages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxPages - 1);

    if (endPage - startPage < maxPages - 1) {
        startPage = Math.max(0, endPage - maxPages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
        paginationHTML += `
            <button class="btn-page ${i === pageNumber ? 'active' : ''}" 
                    onclick="changePage(${i})">
                ${i + 1}
            </button>
        `;
    }

    paginationHTML += `
        <button class="btn-page" 
                onclick="changePage(${pageNumber + 1})" 
                ${last ? 'disabled' : ''}>
            Siguiente <i class="fas fa-chevron-right"></i>
        </button>
    `;

    paginationEl.innerHTML = paginationHTML;
}

function changePage(page) {
    currentPage = page;
    loadProducts();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ========================================
// FILTROS
// ========================================

async function loadCategories() {
    try {
        const categories = await API.products.getCategories();
        const categoryFilter = document.getElementById('categoryFilter');
        
        if (categoryFilter && categories.length > 0) {
            categoryFilter.innerHTML = `
                <option value="">Todas las categorías</option>
                ${categories.map(cat => `
                    <option value="${cat.slug}">${cat.name}</option>
                `).join('')}
            `;
        }
    } catch (error) {
        console.error('Error cargando categorías:', error);
    }
}

function applyFilters() {
    // Obtener valores de filtros
    const categoryFilter = document.getElementById('categoryFilter');
    const minPriceFilter = document.getElementById('minPrice');
    const maxPriceFilter = document.getElementById('maxPrice');
    const sortFilter = document.getElementById('sortFilter');

    if (categoryFilter) {
        currentFilters.category = categoryFilter.value || null;
    }

    if (minPriceFilter) {
        currentFilters.minPrice = minPriceFilter.value ? parseFloat(minPriceFilter.value) : null;
    }

    if (maxPriceFilter) {
        currentFilters.maxPrice = maxPriceFilter.value ? parseFloat(maxPriceFilter.value) : null;
    }

    if (sortFilter) {
        const [sortBy, sortDir] = sortFilter.value.split('-');
        currentFilters.sortBy = sortBy;
        currentFilters.sortDir = sortDir;
    }

    // Resetear a primera página
    currentPage = 0;
    
    // Recargar productos
    loadProducts();
}

function clearFilters() {
    // Resetear filtros
    currentFilters = {
        category: null,
        minPrice: null,
        maxPrice: null,
        search: null,
        sortBy: 'createdAt',
        sortDir: 'DESC'
    };

    // Limpiar inputs
    const categoryFilter = document.getElementById('categoryFilter');
    const minPriceFilter = document.getElementById('minPrice');
    const maxPriceFilter = document.getElementById('maxPrice');
    const sortFilter = document.getElementById('sortFilter');
    const searchInput = document.getElementById('searchInput');

    if (categoryFilter) categoryFilter.value = '';
    if (minPriceFilter) minPriceFilter.value = '';
    if (maxPriceFilter) maxPriceFilter.value = '';
    if (sortFilter) sortFilter.value = 'createdAt-DESC';
    if (searchInput) searchInput.value = '';

    // Recargar productos
    currentPage = 0;
    loadProducts();
}

// ========================================
// BÚSQUEDA
// ========================================

function setupSearch() {
    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');

    if (searchInput) {
        // Búsqueda al presionar Enter
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                performSearch();
            }
        });
    }

    if (searchBtn) {
        searchBtn.addEventListener('click', performSearch);
    }
}

function performSearch() {
    const searchInput = document.getElementById('searchInput');
    if (!searchInput) return;

    const query = searchInput.value.trim();
    currentFilters.search = query || null;
    currentPage = 0;
    loadProducts();
}

// ========================================
// ACCIONES DE PRODUCTOS
// ========================================

function viewProduct(productId) {
    window.location.href = `/product.html?id=${productId}`;
}

async function addToCart(productId) {
    try {
        // Obtener producto completo desde la API
        const product = await API.products.getProductById(productId);

        // Verificar stock
        if (product.stock === 0) {
            showNotification('Producto agotado', 'error');
            return;
        }

        // Agregar al carrito
        API.cart.addItem(product, 1);

        // Mostrar notificación
        showNotification('Producto agregado al carrito', 'success');

        // Opcional: Animación en el botón
        const btn = event.target.closest('button');
        if (btn) {
            btn.classList.add('btn-success');
            setTimeout(() => {
                btn.classList.remove('btn-success');
            }, 1000);
        }

    } catch (error) {
        console.error('Error agregando al carrito:', error);
        showNotification('Error al agregar producto', 'error');
    }
}

// ========================================
// INICIALIZACIÓN
// ========================================

document.addEventListener('DOMContentLoaded', async () => {
    // Cargar categorías para el filtro
    await loadCategories();

    // Setup search
    setupSearch();

    // Setup event listeners para filtros
    const categoryFilter = document.getElementById('categoryFilter');
    const minPriceFilter = document.getElementById('minPrice');
    const maxPriceFilter = document.getElementById('maxPrice');
    const sortFilter = document.getElementById('sortFilter');
    const clearFiltersBtn = document.getElementById('clearFilters');

    if (categoryFilter) categoryFilter.addEventListener('change', applyFilters);
    if (minPriceFilter) minPriceFilter.addEventListener('change', applyFilters);
    if (maxPriceFilter) maxPriceFilter.addEventListener('change', applyFilters);
    if (sortFilter) sortFilter.addEventListener('change', applyFilters);
    if (clearFiltersBtn) clearFiltersBtn.addEventListener('click', clearFilters);

    // Cargar productos iniciales
    loadProducts();
});

// Hacer funciones disponibles globalmente
window.viewProduct = viewProduct;
window.addToCart = addToCart;
window.changePage = changePage;