# AGUARDI - Tienda de Ropa Infantil y Fiesta

![Estado](https://img.shields.io/badge/Estado-En%20Desarrollo-yellow)
![Versión](https://img.shields.io/badge/Versión-1.0.0-blue)

## 📋 Descripción

Sitio web estático para AGUARDI, local de ropa infantil y de fiesta especializado en trajes, vestidos, corbatas, tiradores, moños y accesorios. Frontend desarrollado en HTML, CSS y JavaScript puro, diseñado con una interfaz moderna y temática festiva en tonos crema y amarillo pastel.

## 🎨 Paleta de Colores

```css
--cream: #FFF5E6          /* Fondo principal */
--pastel-yellow: #F9E7B8  /* Acentos suaves */
--accent-yellow: #F4D58A  /* Acentos principales */
--dark-gold: #8B6914      /* Textos y botones */
--deep-gold: #6B5310      /* Hover states */
```

## 📁 Estructura del Proyecto

```
aguardi/
├── index.html                 # Landing page principal
├── catalogo.html             # Página de catálogo (próximamente)
├── producto.html             # Página de producto individual (próximamente)
├── cart.html                 # Carrito de compras (próximamente)
├── login.html                # Inicio de sesión (próximamente)
├── register.html             # Registro de usuario (próximamente)
├── styles/
│   ├── main.css              # Estilos principales ✅
│   ├── catalogo.css          # Estilos del catálogo (próximamente)
│   ├── producto.css          # Estilos de producto (próximamente)
│   └── auth.css              # Estilos de login/register (próximamente)
├── scripts/
│   ├── main.js               # JavaScript principal ✅
│   ├── catalogo.js           # Lógica del catálogo (próximamente)
│   ├── producto.js           # Lógica de producto (próximamente)
│   ├── cart.js               # Lógica del carrito (próximamente)
│   └── auth.js               # Lógica de autenticación (próximamente)
├── data/
│   └── products.json         # Productos de ejemplo (próximamente)
├── assets/
│   └── images/               # Imágenes del sitio (próximamente)
├── admin/
│   ├── index.html            # Panel admin (próximamente)
│   ├── styles/
│   │   └── admin.css         # Estilos admin (próximamente)
│   └── scripts/
│       └── admin.js          # Lógica admin (próximamente)
└── README.md                 # Este archivo ✅
```

## 🚀 Instalación y Uso Local

### Requisitos Previos

- Navegador web moderno (Chrome, Firefox, Edge, Safari)
- Editor de código (VS Code recomendado)
- Servidor local opcional (Live Server, Python SimpleHTTPServer, etc.)

### Pasos de Instalación

1. **Clonar o descargar el proyecto**
   ```bash
   # Si tienes git
   git clone [url-del-repositorio]
   cd aguardi
   ```

2. **Crear la estructura de carpetas**
   ```bash
   mkdir -p styles scripts data assets/images admin/styles admin/scripts
   ```

3. **Copiar los archivos**
   - Copiar `index.html` en la raíz
   - Copiar `main.css` en `styles/`
   - Copiar `main.js` en `scripts/`

4. **Abrir el proyecto**
   
   **Opción A: Directamente en el navegador**
   - Hacer doble clic en `index.html`
   
   **Opción B: Con Live Server (VS Code)**
   - Instalar extensión "Live Server"
   - Click derecho en `index.html` → "Open with Live Server"
   
   **Opción C: Con Python**
   ```bash
   # Python 3
   python -m http.server 8000
   # Abrir http://localhost:8000
   ```

## ✨ Características Implementadas

### ✅ Versión Actual (v1.0.0)

- **Navegación responsive** con menú hamburguesa para móvil
- **Landing page** completa con hero section
- **Categorías** (Bebé, Niño, Niña, Fiesta)
- **Productos destacados** con badges (Nuevo/Oferta)
- **Sistema de carrito** básico con localStorage
- **Contador de carrito** en navbar
- **Notificaciones** al agregar productos
- **Footer** completo con links de contacto
- **Diseño mobile-first** totalmente responsive
- **Accesibilidad** con ARIA labels y navegación por teclado
- **Smooth scroll** para navegación interna

### 🔜 Próximas Características

- Página de catálogo con filtros y búsqueda
- Página de producto individual con galería
- Carrito funcional con checkout
- Sistema de login/registro
- Panel de administración
- Integración con WhatsApp
- Base de datos y backend

## 🎯 Funcionalidades JavaScript

El archivo `scripts/main.js` expone un objeto global `AGUARDI` con las siguientes funciones:

```javascript
// Gestión del carrito
AGUARDI.getCart()                    // Obtener carrito actual
AGUARDI.addToCart(product, quantity) // Agregar producto
AGUARDI.saveCart(cart)               // Guardar carrito

// Gestión de usuario
AGUARDI.getCurrentUser()             // Usuario actual
AGUARDI.isLoggedIn()                 // Verificar login

// Utilidades
AGUARDI.showNotification(msg, type)  // Mostrar notificación
AGUARDI.getStorage(key, default)     // Leer localStorage
AGUARDI.setStorage(key, value)       // Escribir localStorage
```

## 💾 Estructura de Datos

### LocalStorage

El sitio utiliza localStorage para persistencia de datos:

```javascript
// Carrito
localStorage.setItem('aguardi_cart', JSON.stringify([
  {
    id: 1,
    name: "Producto",
    price: 12990,
    quantity: 2,
    category: "nino"
  }
]))

// Usuario
localStorage.setItem('aguardi_user', JSON.stringify({
  id: 1,
  name: "Juan Pérez",
  email: "juan@example.com",
  logged: true,
  role: "client" // o "admin"
}))
```

### Resetear Datos

Para limpiar los datos almacenados localmente:

```javascript
// Desde la consola del navegador (F12)
localStorage.clear()
// O específicamente:
localStorage.removeItem('aguardi_cart')
localStorage.removeItem('aguardi_user')
```

## 📱 Responsive Design

El diseño utiliza un enfoque mobile-first con los siguientes breakpoints:

```css
/* Mobile: < 768px (por defecto) */

/* Tablet: 768px+ */
@media (min-width: 768px) { }

/* Desktop: 1024px+ */
@media (min-width: 1024px) { }

/* Large Desktop: 1280px+ */
@media (min-width: 1280px) { }
```

## ♿ Accesibilidad

### Características de Accesibilidad

- ✅ Etiquetas semánticas HTML5
- ✅ ARIA labels en iconos y botones
- ✅ Alt text en imágenes (cuando se agreguen)
- ✅ Contraste de colores WCAG AA
- ✅ Navegación por teclado
- ✅ Focus visible en elementos interactivos
- ✅ Soporte para `prefers-reduced-motion`
- ✅ Soporte para `prefers-contrast: high`

### Navegación por Teclado

- `Tab` - Navegar entre elementos
- `Enter` / `Space` - Activar botones/links
- `Esc` - Cerrar menú móvil (próximamente)

## 🔒 Seguridad

### ⚠️ Notas Importantes

**MOCK DE DESARROLLO**: El sistema actual de autenticación es solo para desarrollo local y **NO ES SEGURO** para producción.

**No usar en producción sin:**
- Backend con autenticación real
- Hashing de contraseñas (bcrypt, argon2)
- Tokens JWT con HttpOnly cookies
- HTTPS obligatorio
- Validación de datos server-side
- Protección CSRF
- Rate limiting

## 🚀 Próximos Pasos - Migración a Backend

### Plan de Implementación (5 Fases)

#### Fase 1: Preparación del Frontend
- [ ] Separar configuración de API en archivo config.js
- [ ] Implementar manejo de errores de red
- [ ] Agregar loaders y estados de carga
- [ ] Preparar formularios con validación

#### Fase 2: Backend - Autenticación
- [ ] Configurar servidor Node.js + Express
- [ ] Implementar registro con bcrypt
- [ ] Sistema de login con JWT
- [ ] Middleware de autenticación
- [ ] Endpoints: `/api/auth/register`, `/api/auth/login`, `/api/auth/logout`

#### Fase 3: Backend - Productos
- [ ] Base de datos (PostgreSQL/MySQL/MongoDB)
- [ ] Modelo de Productos
- [ ] CRUD completo de productos
- [ ] Upload de imágenes (Cloudinary/S3)
- [ ] Endpoints: `/api/products`, `/api/products/:id`

#### Fase 4: Backend - Pedidos
- [ ] Modelo de Pedidos
- [ ] Relación Usuario-Pedidos
- [ ] Estados de pedido
- [ ] Integración con MercadoPago/Stripe
- [ ] Endpoints: `/api/orders`, `/api/orders/:id`

#### Fase 5: Deployment y Seguridad
- [ ] Variables de entorno (.env)
- [ ] HTTPS con Let's Encrypt
- [ ] CORS configurado
- [ ] Rate limiting
- [ ] Logs y monitoreo
- [ ] Backups automáticos
- [ ] Deploy en Vercel/Heroku/Railway

### Ejemplo de Endpoints API

```javascript
// Autenticación
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me

// Productos
GET    /api/products
GET    /api/products/:id
POST   /api/products         // Admin only
PUT    /api/products/:id     // Admin only
DELETE /api/products/:id     // Admin only

// Carrito
GET    /api/cart
POST   /api/cart/add
PUT    /api/cart/:id
DELETE /api/cart/:id

// Pedidos
GET    /api/orders           // User: sus pedidos, Admin: todos
POST   /api/orders           // Crear pedido
GET    /api/orders/:id
PUT    /api/orders/:id       // Admin: cambiar estado
```

### Ejemplo de Payload

```json
// POST /api/auth/register
{
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "password": "SecurePass123!",
  "phone": "+5492234567890"
}

// POST /api/products (Admin)
{
  "name": "Traje Elegante Niño",
  "description": "Perfecto para bodas",
  "price": 12990,
  "category": "nino",
  "stock": 15,
  "images": ["url1", "url2"],
  "weight": 0.5
}

// POST /api/orders
{
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ],
  "shipping": {
    "name": "Juan Pérez",
    "address": "Calle Falsa 123",
    "city": "Mar del Plata",
    "province": "Buenos Aires",
    "postalCode": "7600",
    "phone": "+5492234567890"
  },
  "paymentMethod": "mercadopago"
}
```

## 📊 Checklist de Testing Manual

Antes de pasar a producción, verificar:

### Funcionalidades Básicas
- [ ] Navegación entre páginas funciona
- [ ] Menú móvil abre y cierra correctamente
- [ ] Productos se muestran correctamente
- [ ] Agregar al carrito funciona
- [ ] Contador de carrito se actualiza
- [ ] Notificaciones aparecen y desaparecen

### Responsive
- [ ] Mobile (< 768px) - iPhone/Android
- [ ] Tablet (768px - 1023px) - iPad
- [ ] Desktop (1024px+) - Laptop/PC
- [ ] Large Desktop (1280px+) - Monitores grandes

### Navegadores
- [ ] Chrome/Edge (Chromium)
- [ ] Firefox
- [ ] Safari (macOS/iOS)
- [ ] Samsung Internet (Android)

### Accesibilidad
- [ ] Navegación por teclado funciona
- [ ] Screen reader (NVDA/JAWS/VoiceOver)
- [ ] Contraste de colores adecuado
- [ ] Imágenes tienen alt text

### Performance
- [ ] Tiempo de carga < 3 segundos
- [ ] No hay errores en consola
- [ ] No hay warnings de accesibilidad
- [ ] Imágenes optimizadas

## 🛠️ Tecnologías Utilizadas

- **HTML5** - Estructura semántica
- **CSS3** - Estilos y animaciones
  - Variables CSS (Custom Properties)
  - Flexbox y Grid
  - Media Queries
- **JavaScript ES6+** - Lógica del cliente
  - Módulos IIFE
  - LocalStorage API
  - Fetch API (para futuro backend)
- **Sin frameworks** - Vanilla JS

## 🌐 Deployment Sugerido

### Opciones Gratuitas

1. **Netlify** (Recomendado para frontend)
   - Drag & drop deployment
   - HTTPS automático
   - CDN global

2. **Vercel**
   - Integración con Git
   - Preview deployments
   - Excelente para Next.js (futuro)

3. **GitHub Pages**
   - Gratis para repos públicos
   - Deploy automático desde main
   - Dominio personalizado disponible

### Backend (cuando esté listo)

- **Railway** - Node.js + PostgreSQL
- **Render** - Full-stack hosting
- **Heroku** - Clásico para Node.js
- **DigitalOcean** - VPS para control total

## 📝 Notas del Desarrollador

### Decisiones de Diseño

- **Sin jQuery**: JavaScript vanilla para mejor performance
- **Mobile-first**: La mayoría del tráfico viene de móviles
- **LocalStorage**: Suficiente para MVP, migrar a backend después
- **Sin preprocesadores**: CSS puro más simple de mantener
- **Sin build tools**: Implementación directa, agregar Webpack después

### Mejoras Futuras

- [ ] Implementar Service Worker para PWA
- [ ] Agregar lazy loading de imágenes
- [ ] Implementar skeleton screens
- [ ] Agregar animaciones con Intersection Observer
- [ ] Optimizar con Webpack/Vite
- [ ] Implementar tests con Jest
- [ ] Agregar Storybook para componentes

## 📄 Licencia

Copyright © 2025 AGUARDI. Todos los derechos reservados.

## 👥 Contacto

- **Email**: info@aguardi.com
- **WhatsApp**: +54 9 223 XXX-XXXX
- **Ubicación**: Mar del Plata, Buenos Aires, Argentina

---

**Versión**: 1.0.0  
**Última actualización**: Noviembre 2025  
**Estado**: Frontend base completado ✅ | Backend pendiente ⏳