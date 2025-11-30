// FILE: scripts/main.js
// Propósito: JavaScript principal para AGUARDI
// Descripción: Manejo de navegación, carrito y funcionalidades básicas
// Uso: Importar en index.html y otras páginas principales

// ============================================
// FILE: frontend/scripts/main.js (ACTUALIZADO)
// Propósito: Funcionalidades globales y página de inicio conectada con el backend
// ============================================

// ========================================
// CARGAR PRODUCTOS DESTACADOS
// ========================================

async function loadFeaturedProducts() {
    try {
        // Cargar productos nuevos y en oferta
        const [newProducts, saleProducts] = await Promise.all([
            API.products.getProducts({
                page: 0,
                size: 4,
                sortBy: 'createdAt',
                sortDir: 'DESC'
            }),
            API.products.getProducts({
                page: 0,
                size: 4,
                onSale: true
            })
        ]);

        // Renderizar productos nuevos
        if (newProducts.content && newProducts.content.length > 0) {
            renderProductSection('newProducts', newProducts.content, 'Nuevos Productos');
        }

        // Renderizar productos en oferta
        if (saleProducts.content && saleProducts.content.length > 0) {
            renderProductSection('saleProducts', saleProducts.content, '¡Ofertas Especiales!');
        }

    } catch (error) {
        console.error('Error cargando productos destacados:', error);
    }
}

// ========================================
// RENDERIZAR SECCIÓN DE PRODUCTOS
// ========================================

function renderProductSection(containerId, products, title) {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = `
        <div class="section-header">
            <h2>${title}</h2>
            <a href="/catalog.html" class="view-all-link">Ver todos <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="products-grid">
            ${products.map(product => `
                <div class="product-card" onclick="window.location.href='/product.html?id=${product.id}'">
                    <div class="product-image">
                        <img src="${product.mainImageUrl || '/images/placeholder.jpg'}" 
                             alt="${product.name}"
                             onerror="this.src='/images/placeholder.jpg'">
                        ${product.onSale ? '<span class="badge badge-sale">OFERTA</span>' : ''}
                        ${product.isNew ? '<span class="badge badge-new">NUEVO</span>' : ''}
                        ${product.stock === 0 ? '<span class="badge badge-sold-out">AGOTADO</span>' : ''}
                    </div>
                    <div class="product-info">
                        <h3 class="product-name">${product.name}</h3>
                        <p class="product-category">${product.categoryName || ''}</p>
                        <div class="product-price">
                            ${product.onSale && product.salePrice ? `
                                <span class="price-old">$${product.price.toFixed(2)}</span>
                                <span class="price-current">$${product.salePrice.toFixed(2)}</span>
                            ` : `
                                <span class="price-current">$${product.price.toFixed(2)}</span>
                            `}
                        </div>
                        <button class="btn btn-add-to-cart" 
                                onclick="event.stopPropagation(); quickAddToCart(${product.id})"
                                ${product.stock === 0 ? 'disabled' : ''}>
                            <i class="fas fa-shopping-cart"></i>
                            ${product.stock === 0 ? 'Agotado' : 'Agregar'}
                        </button>
                    </div>
                </div>
            `).join('')}
        </div>
    `;
}

// ========================================
// AGREGAR RÁPIDO AL CARRITO
// ========================================

async function quickAddToCart(productId) {
    try {
        // Obtener producto
        const product = await API.products.getProductById(productId);

        // Verificar stock
        if (product.stock === 0) {
            showNotification('Producto agotado', 'error');
            return;
        }

        // Agregar al carrito
        API.cart.addItem(product, 1);

        // Notificación
        showNotification('Producto agregado al carrito', 'success');

        // Animación del botón
        const btn = event.target.closest('button');
        if (btn) {
            const originalHTML = btn.innerHTML;
            btn.innerHTML = '<i class="fas fa-check"></i> Agregado';
            btn.classList.add('btn-success');
            
            setTimeout(() => {
                btn.innerHTML = originalHTML;
                btn.classList.remove('btn-success');
            }, 2000);
        }

    } catch (error) {
        console.error('Error agregando producto:', error);
        showNotification('Error al agregar producto', 'error');
    }
}

// ========================================
// CARGAR CATEGORÍAS
// ========================================

async function loadCategories() {
    try {
        const categories = await API.products.getCategories();
        
        const categoriesContainer = document.getElementById('categoriesGrid');
        if (!categoriesContainer || categories.length === 0) return;

        categoriesContainer.innerHTML = categories.map(category => `
            <a href="/catalog.html?category=${category.slug}" class="category-card">
                <div class="category-image">
                    <img src="${category.imageUrl || '/images/categories/default.jpg'}" 
                         alt="${category.name}"
                         onerror="this.src='/images/categories/default.jpg'">
                </div>
                <h3>${category.name}</h3>
                ${category.description ? `<p>${category.description}</p>` : ''}
            </a>
        `).join('');

    } catch (error) {
        console.error('Error cargando categorías:', error);
    }
}

// ========================================
// HERO SLIDER (SI EXISTE)
// ========================================

let currentSlide = 0;
let slideInterval;

function initHeroSlider() {
    const slider = document.getElementById('heroSlider');
    if (!slider) return;

    const slides = slider.querySelectorAll('.slide');
    if (slides.length === 0) return;

    // Mostrar primer slide
    showSlide(0);

    // Auto-play
    slideInterval = setInterval(() => {
        nextSlide();
    }, 5000);

    // Botones de navegación
    const prevBtn = document.getElementById('prevSlide');
    const nextBtn = document.getElementById('nextSlide');

    if (prevBtn) prevBtn.addEventListener('click', prevSlide);
    if (nextBtn) nextBtn.addEventListener('click', nextSlide);

    // Pausar en hover
    slider.addEventListener('mouseenter', () => {
        clearInterval(slideInterval);
    });

    slider.addEventListener('mouseleave', () => {
        slideInterval = setInterval(nextSlide, 5000);
    });
}

function showSlide(index) {
    const slides = document.querySelectorAll('.slide');
    if (slides.length === 0) return;

    currentSlide = index;
    
    if (currentSlide >= slides.length) currentSlide = 0;
    if (currentSlide < 0) currentSlide = slides.length - 1;

    slides.forEach((slide, i) => {
        slide.classList.remove('active');
        if (i === currentSlide) {
            slide.classList.add('active');
        }
    });

    // Actualizar indicadores
    const indicators = document.querySelectorAll('.slide-indicator');
    indicators.forEach((indicator, i) => {
        indicator.classList.remove('active');
        if (i === currentSlide) {
            indicator.classList.add('active');
        }
    });
}

function nextSlide() {
    showSlide(currentSlide + 1);
}

function prevSlide() {
    showSlide(currentSlide - 1);
}

// ========================================
// NEWSLETTER
// ========================================

function setupNewsletterForm() {
    const form = document.getElementById('newsletterForm');
    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const emailInput = form.querySelector('input[type="email"]');
        const submitBtn = form.querySelector('button[type="submit"]');
        
        if (!emailInput) return;

        const email = emailInput.value;
        const originalBtnText = submitBtn.textContent;

        try {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Suscribiendo...';

            // TODO: Implementar endpoint de newsletter en el backend
            // await API.newsletter.subscribe(email);

            // Por ahora, simulamos
            await new Promise(resolve => setTimeout(resolve, 1000));

            showNotification('¡Gracias por suscribirte!', 'success');
            form.reset();

        } catch (error) {
            console.error('Error en newsletter:', error);
            showNotification('Error al suscribirse', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = originalBtnText;
        }
    });
}

// ========================================
// BÚSQUEDA RÁPIDA
// ========================================

function setupQuickSearch() {
    const searchInput = document.getElementById('quickSearch');
    if (!searchInput) return;

    let searchTimeout;

    searchInput.addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        
        const query = e.target.value.trim();
        
        if (query.length < 3) {
            hideSearchResults();
            return;
        }

        searchTimeout = setTimeout(() => {
            performQuickSearch(query);
        }, 300);
    });

    // Cerrar resultados al hacer click fuera
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.search-container')) {
            hideSearchResults();
        }
    });
}

async function performQuickSearch(query) {
    try {
        const response = await API.products.searchProducts(query, {
            page: 0,
            size: 5
        });

        showSearchResults(response.content);

    } catch (error) {
        console.error('Error en búsqueda:', error);
    }
}

function showSearchResults(products) {
    let resultsContainer = document.getElementById('searchResults');
    
    if (!resultsContainer) {
        resultsContainer = document.createElement('div');
        resultsContainer.id = 'searchResults';
        resultsContainer.className = 'search-results';
        
        const searchContainer = document.querySelector('.search-container');
        if (searchContainer) {
            searchContainer.appendChild(resultsContainer);
        }
    }

    if (!products || products.length === 0) {
        resultsContainer.innerHTML = '<div class="no-results">No se encontraron productos</div>';
        resultsContainer.style.display = 'block';
        return;
    }

    resultsContainer.innerHTML = products.map(product => `
        <a href="/product.html?id=${product.id}" class="search-result-item">
            <img src="${product.mainImageUrl || '/images/placeholder.jpg'}" 
                 alt="${product.name}">
            <div class="search-result-info">
                <strong>${product.name}</strong>
                <span class="price">$${(product.salePrice || product.price).toFixed(2)}</span>
            </div>
        </a>
    `).join('');

    resultsContainer.style.display = 'block';
}

function hideSearchResults() {
    const resultsContainer = document.getElementById('searchResults');
    if (resultsContainer) {
        resultsContainer.style.display = 'none';
    }
}

// ========================================
// SCROLL TO TOP
// ========================================

function setupScrollToTop() {
    const scrollBtn = document.getElementById('scrollToTop');
    if (!scrollBtn) return;

    window.addEventListener('scroll', () => {
        if (window.pageYOffset > 300) {
            scrollBtn.style.display = 'block';
        } else {
            scrollBtn.style.display = 'none';
        }
    });

    scrollBtn.addEventListener('click', () => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
}

// ========================================
// MENU MOBILE
// ========================================

function setupMobileMenu() {
    const menuToggle = document.getElementById('mobileMenuToggle');
    const mobileMenu = document.getElementById('mobileMenu');
    
    if (!menuToggle || !mobileMenu) return;

    menuToggle.addEventListener('click', () => {
        mobileMenu.classList.toggle('active');
        menuToggle.classList.toggle('active');
    });

    // Cerrar al hacer click en un link
    const menuLinks = mobileMenu.querySelectorAll('a');
    menuLinks.forEach(link => {
        link.addEventListener('click', () => {
            mobileMenu.classList.remove('active');
            menuToggle.classList.remove('active');
        });
    });
}

// ========================================
// ESTADÍSTICAS (OPCIONAL)
// ========================================

function animateStats() {
    const stats = document.querySelectorAll('.stat-number');
    
    stats.forEach(stat => {
        const target = parseInt(stat.getAttribute('data-target'));
        const duration = 2000;
        const increment = target / (duration / 16);
        let current = 0;

        const updateStat = () => {
            current += increment;
            if (current < target) {
                stat.textContent = Math.floor(current);
                requestAnimationFrame(updateStat);
            } else {
                stat.textContent = target;
            }
        };

        // Animar cuando sea visible
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    updateStat();
                    observer.disconnect();
                }
            });
        });

        observer.observe(stat);
    });
}

// ========================================
// INICIALIZACIÓN
// ========================================

document.addEventListener('DOMContentLoaded', async () => {
    // Actualizar UI de autenticación
    if (typeof updateAuthUI === 'function') {
        await updateAuthUI();
    }

    // Actualizar contador del carrito
    if (API && API.cart) {
        API.cart.updateCartCount();
    }

    // Cargar contenido de la página de inicio
    if (document.getElementById('newProducts') || document.getElementById('saleProducts')) {
        await loadFeaturedProducts();
    }

    // Cargar categorías
    if (document.getElementById('categoriesGrid')) {
        await loadCategories();
    }

    // Inicializar hero slider
    initHeroSlider();

    // Setup newsletter
    setupNewsletterForm();

    // Setup búsqueda rápida
    setupQuickSearch();

    // Setup scroll to top
    setupScrollToTop();

    // Setup menú mobile
    setupMobileMenu();

    // Animar estadísticas
    animateStats();
});

// Hacer funciones disponibles globalmente
window.quickAddToCart = quickAddToCart;
window.nextSlide = nextSlide;
window.prevSlide = prevSlide;