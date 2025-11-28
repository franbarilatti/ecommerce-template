// FILE: scripts/catalog.js
// Propósito: Lógica del catálogo con filtros, búsqueda y paginación
// Descripción: Manejo de productos, filtrado dinámico y gestión de estado
// Uso: Importar en catalog.html después de main.js

(function() {
    'use strict';

    // ===== CONFIGURACIÓN =====
    const CONFIG = {
        PRODUCTS_PER_PAGE: 12,
        DEBOUNCE_DELAY: 300, // Milisegundos para debounce en búsqueda
        ANIMATION_DELAY: 50 // Delay entre animaciones de productos
    };

    // ===== MOCK DATA - Productos de ejemplo =====
    // En producción, estos vendrían de data/products.json o una API
    const MOCK_PRODUCTS = [
        {
            id: 1,
            name: 'Traje Elegante Niño',
            description: 'Traje completo perfecto para bodas y eventos especiales',
            price: 12990,
            category: 'boy',
            image: null,
            isNew: true,
            onSale: false,
            stock: 15
        },
        {
            id: 2,
            name: 'Vestido de Fiesta Niña',
            description: 'Elegante vestido para cualquier ocasión especial',
            price: 15990,
            category: 'girl',
            image: null,
            isNew: false,
            onSale: true,
            stock: 8
        },
        {
            id: 3,
            name: 'Set de Accesorios Premium',
            description: 'Moños, tiradores y corbatas de alta calidad',
            price: 3990,
            category: 'accessories',
            image: null,
            isNew: false,
            onSale: false,
            stock: 25
        },
        {
            id: 4,
            name: 'Conjunto Bebé Bautismo',
            description: 'Hermoso conjunto blanco para bautismo',
            price: 8990,
            category: 'baby',
            image: null,
            isNew: true,
            onSale: false,
            stock: 12
        },
        {
            id: 5,
            name: 'Vestido Casual Niña',
            description: 'Cómodo y elegante para el día a día',
            price: 9990,
            category: 'girl',
            image: null,
            isNew: false,
            onSale: true,
            stock: 20
        },
        {
            id: 6,
            name: 'Camisa y Pantalón Niño',
            description: 'Conjunto versátil para diversas ocasiones',
            price: 7990,
            category: 'boy',
            image: null,
            isNew: true,
            onSale: false,
            stock: 18
        },
        {
            id: 7,
            name: 'Vestido Fiesta Largo',
            description: 'Vestido de gala para eventos importantes',
            price: 19990,
            category: 'party',
            image: null,
            isNew: true,
            onSale: false,
            stock: 5
        },
        {
            id: 8,
            name: 'Corbatas Niño Pack x3',
            description: 'Set de 3 corbatas en diferentes colores',
            price: 2990,
            category: 'accessories',
            image: null,
            isNew: false,
            onSale: true,
            stock: 30
        },
        {
            id: 9,
            name: 'Mameluco Bebé',
            description: 'Suave y cómodo para los más pequeños',
            price: 4990,
            category: 'baby',
            image: null,
            isNew: false,
            onSale: false,
            stock: 22
        },
        {
            id: 10,
            name: 'Traje Ceremonia Niño',
            description: 'Traje completo con chaleco para ceremonias',
            price: 16990,
            category: 'party',
            image: null,
            isNew: true,
            onSale: false,
            stock: 10
        },
        {
            id: 11,
            name: 'Vestido Princesa Niña',
            description: 'Diseño de princesa con tul y brillos',
            price: 13990,
            category: 'girl',
            image: null,
            isNew: false,
            onSale: false,
            stock: 14
        },
        {
            id: 12,
            name: 'Moños y Clips Pack',
            description: 'Variedad de moños y clips para niña',
            price: 1990,
            category: 'accessories',
            image: null,
            isNew: false,
            onSale: true,
            stock: 40
        },
        {
            id: 13,
            name: 'Enterito Bebé Algodón',
            description: '100% algodón, ideal para recién nacidos',
            price: 3490,
            category: 'baby',
            image: null,
            isNew: true,
            onSale: false,
            stock: 28
        },
        {
            id: 14,
            name: 'Pantalón Vestir Niño',
            description: 'Pantalón de vestir clásico',
            price: 5990,
            category: 'boy',
            image: null,
            isNew: false,
            onSale: true,
            stock: 16
        },
        {
            id: 15,
            name: 'Vestido Fiesta Corto',
            description: 'Vestido corto ideal para fiestas',
            price: 11990,
            category: 'party',
            image: null,
            isNew: false,
            onSale: false,
            stock: 11
        }
    ];

    // ===== ESTADO DE LA APLICACIÓN =====
    let state = {
        allProducts: [],
        filteredProducts: [],
        currentPage: 1,
        filters: {
            search: '',
            categories: ['all'],
            minPrice: null,
            maxPrice: null,
            isNew: false,
            onSale: false
        },
        sortBy: 'default'
    };

    // ===== ELEMENTOS DEL DOM =====
    const elements = {
        // Filtros
        searchInput: document.getElementById('searchInput'),
        categoryCheckboxes: document.querySelectorAll('input[name="category"]'),
        minPriceInput: document.getElementById('minPrice'),
        maxPriceInput: document.getElementById('maxPrice'),
        applyPriceBtn: document.getElementById('applyPriceBtn'),
        specialCheckboxes: document.querySelectorAll('input[name="special"]'),
        clearFiltersBtn: document.getElementById('clearFiltersBtn'),
        clearFiltersFromEmpty: document.getElementById('clearFiltersFromEmpty'),
        
        // Toolbar
        filterToggleBtn: document.getElementById('filterToggleBtn'),
        sortSelect: document.getElementById('sortSelect'),
        resultsCount: document.getElementById('resultsCount'),
        
        // Productos
        productsGrid: document.getElementById('productsGrid'),
        emptyState: document.getElementById('emptyState'),
        loadingState: document.getElementById('loadingState'),
        
        // Paginación
        pagination: document.getElementById('pagination'),
        paginationPages: document.getElementById('paginationPages'),
        prevPageBtn: document.getElementById('prevPageBtn'),
        nextPageBtn: document.getElementById('nextPageBtn'),
        
        // Sidebar
        sidebar: document.getElementById('catalogSidebar')
    };

    // ===== UTILIDADES =====

    /**
     * Debounce - Retrasa la ejecución de una función
     * @param {Function} func - Función a ejecutar
     * @param {number} wait - Tiempo de espera en ms
     * @returns {Function} Función debounced
     */
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
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

    // ===== CARGA DE PRODUCTOS =====

    /**
     * Cargar productos (simula llamada a API)
     * @returns {Promise<Array>} Array de productos
     */
    async function loadProducts() {
        showLoading();
        
        // Simular delay de red
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // En producción, aquí iría:
        // const response = await fetch('/api/products');
        // const products = await response.json();
        
        return MOCK_PRODUCTS;
    }

    /**
     * Inicializar catálogo
     */
    async function initCatalog() {
        try {
            state.allProducts = await loadProducts();
            state.filteredProducts = [...state.allProducts];
            
            updateCategoryCounts();
            applyFiltersAndSort();
            hideLoading();
        } catch (error) {
            console.error('Error al cargar productos:', error);
            showError();
        }
    }

    // ===== FILTRADO =====

    /**
     * Aplicar todos los filtros y ordenamiento
     */
    function applyFiltersAndSort() {
        let products = [...state.allProducts];
        
        // Filtro de búsqueda
        if (state.filters.search) {
            const search = state.filters.search.toLowerCase();
            products = products.filter(p => 
                p.name.toLowerCase().includes(search) ||
                p.description.toLowerCase().includes(search)
            );
        }
        
        // Filtro de categorías
        if (!state.filters.categories.includes('all')) {
            products = products.filter(p => 
                state.filters.categories.includes(p.category)
            );
        }
        
        // Filtro de precio
        if (state.filters.minPrice !== null) {
            products = products.filter(p => p.price >= state.filters.minPrice);
        }
        if (state.filters.maxPrice !== null) {
            products = products.filter(p => p.price <= state.filters.maxPrice);
        }
        
        // Filtros especiales
        if (state.filters.isNew) {
            products = products.filter(p => p.isNew);
        }
        if (state.filters.onSale) {
            products = products.filter(p => p.onSale);
        }
        
        // Ordenamiento
        products = sortProducts(products, state.sortBy);
        
        state.filteredProducts = products;
        state.currentPage = 1; // Resetear a página 1
        
        renderProducts();
        updateResultsCount();
        renderPagination();
    }

    /**
     * Ordenar productos
     * @param {Array} products - Array de productos
     * @param {string} sortBy - Criterio de ordenamiento
     * @returns {Array} Productos ordenados
     */
    function sortProducts(products, sortBy) {
        const sorted = [...products];
        
        switch (sortBy) {
            case 'price-asc':
                return sorted.sort((a, b) => a.price - b.price);
            case 'price-desc':
                return sorted.sort((a, b) => b.price - a.price);
            case 'name-asc':
                return sorted.sort((a, b) => a.name.localeCompare(b.name));
            case 'name-desc':
                return sorted.sort((a, b) => b.name.localeCompare(a.name));
            case 'newest':
                return sorted.sort((a, b) => (b.isNew ? 1 : 0) - (a.isNew ? 1 : 0));
            default:
                // Default: priorizar nuevos y en oferta
                return sorted.sort((a, b) => {
                    if (a.isNew && !b.isNew) return -1;
                    if (!a.isNew && b.isNew) return 1;
                    if (a.onSale && !b.onSale) return -1;
                    if (!a.onSale && b.onSale) return 1;
                    return 0;
                });
        }
    }

    /**
     * Actualizar contadores de categorías
     */
    function updateCategoryCounts() {
        const counts = {
            all: state.allProducts.length,
            baby: state.allProducts.filter(p => p.category === 'baby').length,
            boy: state.allProducts.filter(p => p.category === 'boy').length,
            girl: state.allProducts.filter(p => p.category === 'girl').length,
            party: state.allProducts.filter(p => p.category === 'party').length,
            accessories: state.allProducts.filter(p => p.category === 'accessories').length
        };
        
        // Actualizar elementos del DOM
        document.getElementById('countAll').textContent = counts.all;
        document.getElementById('countBaby').textContent = counts.baby;
        document.getElementById('countBoy').textContent = counts.boy;
        document.getElementById('countGirl').textContent = counts.girl;
        document.getElementById('countParty').textContent = counts.party;
        document.getElementById('countAccessories').textContent = counts.accessories;
    }

    /**
     * Limpiar todos los filtros
     */
    function clearAllFilters() {
        // Resetear estado
        state.filters = {
            search: '',
            categories: ['all'],
            minPrice: null,
            maxPrice: null,
            isNew: false,
            onSale: false
        };
        
        // Resetear inputs
        elements.searchInput.value = '';
        elements.minPriceInput.value = '';
        elements.maxPriceInput.value = '';
        
        // Resetear checkboxes
        elements.categoryCheckboxes.forEach(cb => {
            cb.checked = cb.value === 'all';
        });
        elements.specialCheckboxes.forEach(cb => {
            cb.checked = false;
        });
        
        applyFiltersAndSort();
    }

    // ===== RENDERIZADO =====

    /**
     * Renderizar productos en el grid
     */
    function renderProducts() {
        const startIndex = (state.currentPage - 1) * CONFIG.PRODUCTS_PER_PAGE;
        const endIndex = startIndex + CONFIG.PRODUCTS_PER_PAGE;
        const productsToShow = state.filteredProducts.slice(startIndex, endIndex);
        
        // Limpiar grid
        elements.productsGrid.innerHTML = '';
        
        // Mostrar estado vacío si no hay productos
        if (productsToShow.length === 0) {
            elements.emptyState.style.display = 'block';
            elements.pagination.style.display = 'none';
            return;
        }
        
        elements.emptyState.style.display = 'none';
        elements.pagination.style.display = 'flex';
        
        // Renderizar cada producto con animación escalonada
        productsToShow.forEach((product, index) => {
            const productCard = createProductCard(product);
            productCard.style.opacity = '0';
            productCard.style.transform = 'translateY(20px)';
            elements.productsGrid.appendChild(productCard);
            
            // Animación de entrada
            setTimeout(() => {
                productCard.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
                productCard.style.opacity = '1';
                productCard.style.transform = 'translateY(0)';
            }, index * CONFIG.ANIMATION_DELAY);
        });
    }

    /**
     * Crear tarjeta de producto
     * @param {Object} product - Datos del producto
     * @returns {HTMLElement} Elemento de la tarjeta
     */
    function createProductCard(product) {
        const card = document.createElement('div');
        card.className = 'product-card';
        
        // Badge si aplica
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
                    <p class="product-description">${product.description}</p>
                    <div class="product-footer">
                        <span class="product-price">$${product.price.toLocaleString('es-AR')}</span>
                        <button class="btn-icon add-to-cart-btn" data-product-id="${product.id}" aria-label="Agregar al carrito">
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
        
        // Event listener para el botón de carrito
        const addToCartBtn = card.querySelector('.add-to-cart-btn');
        addToCartBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            handleAddToCart(product);
        });
        
        return card;
    }

    /**
     * Agregar producto al carrito
     * @param {Object} product - Producto a agregar
     */
    function handleAddToCart(product) {
        if (window.AGUARDI && window.AGUARDI.addToCart) {
            window.AGUARDI.addToCart(product, 1);
        } else {
            console.error('AGUARDI global no encontrado');
        }
    }

    /**
     * Actualizar contador de resultados
     */
    function updateResultsCount() {
        const count = state.filteredProducts.length;
        elements.resultsCount.innerHTML = `
            Mostrando <strong>${count}</strong> producto${count !== 1 ? 's' : ''}
        `;
    }

    /**
     * Renderizar paginación
     */
    function renderPagination() {
        const totalPages = Math.ceil(state.filteredProducts.length / CONFIG.PRODUCTS_PER_PAGE);
        
        if (totalPages <= 1) {
            elements.pagination.style.display = 'none';
            return;
        }
        
        elements.pagination.style.display = 'flex';
        
        // Actualizar botones prev/next
        elements.prevPageBtn.disabled = state.currentPage === 1;
        elements.nextPageBtn.disabled = state.currentPage === totalPages;
        
        // Limpiar páginas
        elements.paginationPages.innerHTML = '';
        
        // Generar números de página
        const pagesToShow = generatePageNumbers(state.currentPage, totalPages);
        
        pagesToShow.forEach(page => {
            if (page === '...') {
                const ellipsis = document.createElement('span');
                ellipsis.className = 'pagination-ellipsis';
                ellipsis.textContent = '...';
                elements.paginationPages.appendChild(ellipsis);
            } else {
                const pageBtn = document.createElement('button');
                pageBtn.className = 'pagination-page';
                pageBtn.textContent = page;
                
                if (page === state.currentPage) {
                    pageBtn.classList.add('active');
                }
                
                pageBtn.addEventListener('click', () => goToPage(page));
                elements.paginationPages.appendChild(pageBtn);
            }
        });
    }

    /**
     * Generar números de página a mostrar
     * @param {number} current - Página actual
     * @param {number} total - Total de páginas
     * @returns {Array} Array con números de página y elipsis
     */
    function generatePageNumbers(current, total) {
        const pages = [];
        
        if (total <= 7) {
            // Mostrar todas las páginas
            for (let i = 1; i <= total; i++) {
                pages.push(i);
            }
        } else {
            // Mostrar con elipsis
            pages.push(1);
            
            if (current > 3) {
                pages.push('...');
            }
            
            for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) {
                pages.push(i);
            }
            
            if (current < total - 2) {
                pages.push('...');
            }
            
            pages.push(total);
        }
        
        return pages;
    }

    /**
     * Ir a página específica
     * @param {number} page - Número de página
     */
    function goToPage(page) {
        state.currentPage = page;
        renderProducts();
        renderPagination();
        
        // Scroll al inicio del grid
        elements.productsGrid.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    // ===== ESTADOS DE CARGA =====

    /**
     * Mostrar estado de carga
     */
    function showLoading() {
        elements.loadingState.style.display = 'block';
        elements.productsGrid.style.display = 'none';
        elements.emptyState.style.display = 'none';
    }

    /**
     * Ocultar estado de carga
     */
    function hideLoading() {
        elements.loadingState.style.display = 'none';
        elements.productsGrid.style.display = 'grid';
    }

    /**
     * Mostrar error
     */
    function showError() {
        hideLoading();
        elements.emptyState.style.display = 'block';
        elements.emptyState.querySelector('.empty-title').textContent = 'Error al cargar productos';
        elements.emptyState.querySelector('.empty-text').textContent = 'Por favor, intenta recargar la página';
    }

    // ===== EVENT LISTENERS =====

    /**
     * Inicializar event listeners
     */
    function initEventListeners() {
        // Búsqueda con debounce
        const debouncedSearch = debounce((value) => {
            state.filters.search = value;
            applyFiltersAndSort();
        }, CONFIG.DEBOUNCE_DELAY);
        
        elements.searchInput.addEventListener('input', (e) => {
            debouncedSearch(e.target.value);
        });
        
        // Checkboxes de categoría
        elements.categoryCheckboxes.forEach(checkbox => {
            checkbox.addEventListener('change', (e) => {
                if (e.target.value === 'all') {
                    // Si se selecciona "Todas", desmarcar las demás
                    if (e.target.checked) {
                        elements.categoryCheckboxes.forEach(cb => {
                            if (cb.value !== 'all') cb.checked = false;
                        });
                        state.filters.categories = ['all'];
                    }
                } else {
                    // Si se selecciona una categoría específica, desmarcar "Todas"
                    const allCheckbox = Array.from(elements.categoryCheckboxes).find(cb => cb.value === 'all');
                    allCheckbox.checked = false;
                    
                    // Actualizar array de categorías
                    const checkedCategories = Array.from(elements.categoryCheckboxes)
                        .filter(cb => cb.checked && cb.value !== 'all')
                        .map(cb => cb.value);
                    
                    state.filters.categories = checkedCategories.length > 0 ? checkedCategories : ['all'];
                    
                    // Si no hay ninguna seleccionada, marcar "Todas"
                    if (checkedCategories.length === 0) {
                        allCheckbox.checked = true;
                    }
                }
                
                applyFiltersAndSort();
            });
        });
        
        // Filtro de precio
        elements.applyPriceBtn.addEventListener('click', () => {
            const minPrice = parseFloat(elements.minPriceInput.value) || null;
            const maxPrice = parseFloat(elements.maxPriceInput.value) || null;
            
            state.filters.minPrice = minPrice;
            state.filters.maxPrice = maxPrice;
            
            applyFiltersAndSort();
        });
        
        // Enter en inputs de precio
        [elements.minPriceInput, elements.maxPriceInput].forEach(input => {
            input.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    elements.applyPriceBtn.click();
                }
            });
        });
        
        // Filtros especiales
        elements.specialCheckboxes.forEach(checkbox => {
            checkbox.addEventListener('change', (e) => {
                if (e.target.value === 'new') {
                    state.filters.isNew = e.target.checked;
                } else if (e.target.value === 'sale') {
                    state.filters.onSale = e.target.checked;
                }
                applyFiltersAndSort();
            });
        });
        
        // Limpiar filtros
        elements.clearFiltersBtn.addEventListener('click', clearAllFilters);
        elements.clearFiltersFromEmpty.addEventListener('click', clearAllFilters);
        
        // Ordenamiento
        elements.sortSelect.addEventListener('change', (e) => {
            state.sortBy = e.target.value;
            applyFiltersAndSort();
        });
        
        // Paginación
        elements.prevPageBtn.addEventListener('click', () => {
            if (state.currentPage > 1) {
                goToPage(state.currentPage - 1);
            }
        });
        
        elements.nextPageBtn.addEventListener('click', () => {
            const totalPages = Math.ceil(state.filteredProducts.length / CONFIG.PRODUCTS_PER_PAGE);
            if (state.currentPage < totalPages) {
                goToPage(state.currentPage + 1);
            }
        });
        
        // Toggle sidebar en móvil
        elements.filterToggleBtn.addEventListener('click', () => {
            elements.sidebar.classList.toggle('active');
        });
        
        // Cerrar sidebar al hacer clic fuera (móvil)
        document.addEventListener('click', (e) => {
            if (window.innerWidth < 768) {
                if (!elements.sidebar.contains(e.target) && 
                    !elements.filterToggleBtn.contains(e.target) &&
                    elements.sidebar.classList.contains('active')) {
                    elements.sidebar.classList.remove('active');
                }
            }
        });
        
        // Manejar parámetros de URL (ej: ?category=baby)
        handleURLParams();
    }

    /**
     * Manejar parámetros de la URL
     */
    function handleURLParams() {
        const urlParams = new URLSearchParams(window.location.search);
        const category = urlParams.get('category');
        
        if (category && category !== 'all') {
            // Desmarcar "Todas"
            const allCheckbox = Array.from(elements.categoryCheckboxes).find(cb => cb.value === 'all');
            if (allCheckbox) allCheckbox.checked = false;
            
            // Marcar la categoría específica
            const categoryCheckbox = Array.from(elements.categoryCheckboxes).find(cb => cb.value === category);
            if (categoryCheckbox) {
                categoryCheckbox.checked = true;
                state.filters.categories = [category];
                applyFiltersAndSort();
            }
        }
    }

    // ===== INICIALIZACIÓN =====

    /**
     * Inicializar el catálogo
     */
    function init() {
        initEventListeners();
        initCatalog();
        
        console.log('Catálogo AGUARDI inicializado');
    }

    // Inicializar cuando el DOM esté listo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();