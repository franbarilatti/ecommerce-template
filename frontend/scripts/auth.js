// ============================================
// FILE: frontend/scripts/auth.js (ACTUALIZADO)
// Propósito: Manejo de autenticación conectado con el backend
// ============================================

// ========================================
// LOGIN
// ========================================

const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const submitBtn = loginForm.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;

        try {
            // Deshabilitar botón
            submitBtn.disabled = true;
            submitBtn.textContent = 'Iniciando sesión...';

            // Obtener datos del formulario
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            // Llamar a la API
            const response = await API.auth.login(email, password);

            // Guardar usuario en localStorage
            localStorage.setItem('user', JSON.stringify(response.user));

            // Mostrar mensaje de éxito
            showNotification('¡Inicio de sesión exitoso!', 'success');

            // Redirigir después de 1 segundo
            setTimeout(() => {
                // Verificar si hay returnUrl en query params
                const urlParams = new URLSearchParams(window.location.search);
                const returnUrl = urlParams.get('returnUrl') || '/index.html';
                window.location.href = returnUrl;
            }, 1000);

        } catch (error) {
            console.error('Error en login:', error);
            
            let errorMessage = 'Error al iniciar sesión. Verifica tus credenciales.';
            
            if (error.status === 401) {
                errorMessage = 'Email o contraseña incorrectos';
            } else if (error.status === 403) {
                errorMessage = 'Tu cuenta está deshabilitada. Contacta al soporte.';
            } else if (error.message) {
                errorMessage = error.message;
            }

            showNotification(errorMessage, 'error');

            // Re-habilitar botón
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    });
}

// ========================================
// REGISTRO
// ========================================

const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const submitBtn = registerForm.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;

        try {
            // Validar contraseñas
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (password !== confirmPassword) {
                showNotification('Las contraseñas no coinciden', 'error');
                return;
            }

            // Validar longitud de contraseña
            if (password.length < 6) {
                showNotification('La contraseña debe tener al menos 6 caracteres', 'error');
                return;
            }

            // Deshabilitar botón
            submitBtn.disabled = true;
            submitBtn.textContent = 'Registrando...';

            // Obtener datos del formulario
            const userData = {
                firstName: document.getElementById('firstName').value,
                lastName: document.getElementById('lastName').value,
                email: document.getElementById('email').value,
                password: password,
                phone: document.getElementById('phone')?.value || ''
            };

            // Llamar a la API
            const response = await API.auth.register(userData);

            // Guardar usuario en localStorage
            localStorage.setItem('user', JSON.stringify(response.user));

            // Mostrar mensaje de éxito
            showNotification('¡Registro exitoso! Bienvenido a AGUARDI', 'success');

            // Redirigir después de 1.5 segundos
            setTimeout(() => {
                window.location.href = '/index.html';
            }, 1500);

        } catch (error) {
            console.error('Error en registro:', error);
            
            let errorMessage = 'Error al registrarse. Intenta nuevamente.';
            
            if (error.status === 409) {
                errorMessage = 'Este email ya está registrado';
            } else if (error.message) {
                errorMessage = error.message;
            }

            showNotification(errorMessage, 'error');

            // Re-habilitar botón
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    });
}

// ========================================
// LOGOUT
// ========================================

function logout() {
    if (confirm('¿Estás seguro que deseas cerrar sesión?')) {
        API.auth.logout();
    }
}

// ========================================
// FORGOT PASSWORD
// ========================================

const forgotPasswordForm = document.getElementById('forgotPasswordForm');
if (forgotPasswordForm) {
    forgotPasswordForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const submitBtn = forgotPasswordForm.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;

        try {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Enviando...';

            const email = document.getElementById('email').value;

            await API.auth.forgotPassword(email);

            showNotification(
                'Se ha enviado un email con instrucciones para restablecer tu contraseña',
                'success'
            );

            // Limpiar formulario
            forgotPasswordForm.reset();

        } catch (error) {
            console.error('Error:', error);
            showNotification(
                error.message || 'Error al enviar email de recuperación',
                'error'
            );
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    });
}

// ========================================
// RESET PASSWORD
// ========================================

const resetPasswordForm = document.getElementById('resetPasswordForm');
if (resetPasswordForm) {
    resetPasswordForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const submitBtn = resetPasswordForm.querySelector('button[type="submit"]');
        const originalText = submitBtn.textContent;

        try {
            // Validar contraseñas
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (newPassword !== confirmPassword) {
                showNotification('Las contraseñas no coinciden', 'error');
                return;
            }

            if (newPassword.length < 6) {
                showNotification('La contraseña debe tener al menos 6 caracteres', 'error');
                return;
            }

            submitBtn.disabled = true;
            submitBtn.textContent = 'Restableciendo...';

            // Obtener token de URL
            const urlParams = new URLSearchParams(window.location.search);
            const token = urlParams.get('token');

            if (!token) {
                showNotification('Token inválido o expirado', 'error');
                return;
            }

            await API.auth.resetPassword(token, newPassword);

            showNotification('Contraseña restablecida exitosamente', 'success');

            // Redirigir a login después de 2 segundos
            setTimeout(() => {
                window.location.href = '/login.html';
            }, 2000);

        } catch (error) {
            console.error('Error:', error);
            showNotification(
                error.message || 'Error al restablecer contraseña',
                'error'
            );
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    });
}

// ========================================
// CHECK AUTH STATUS
// ========================================

function checkAuthStatus() {
    const token = API.token.getToken();
    const currentPage = window.location.pathname;

    // Páginas que requieren autenticación
    const protectedPages = ['/checkout.html', '/profile.html', '/admin/'];

    // Si está en página protegida y no tiene token, redirigir a login
    if (protectedPages.some(page => currentPage.includes(page)) && !token) {
        const returnUrl = encodeURIComponent(currentPage);
        window.location.href = `/login.html?returnUrl=${returnUrl}`;
        return false;
    }

    // Si está en login/register y tiene token, redirigir a home
    const authPages = ['/login.html', '/register.html'];
    if (authPages.some(page => currentPage.includes(page)) && token) {
        window.location.href = '/index.html';
        return false;
    }

    return true;
}

// ========================================
// UPDATE UI BASED ON AUTH
// ========================================

async function updateAuthUI() {
    const token = API.token.getToken();
    const user = JSON.parse(localStorage.getItem('user') || 'null');

    // Elementos de UI
    const authButtons = document.querySelectorAll('.auth-buttons');
    const userMenus = document.querySelectorAll('.user-menu');
    const userNameElements = document.querySelectorAll('.user-name');

    if (token && user) {
        // Usuario autenticado
        authButtons.forEach(el => el.style.display = 'none');
        userMenus.forEach(el => el.style.display = 'block');
        userNameElements.forEach(el => el.textContent = user.firstName || user.email);

        // Si el usuario no está cargado en localStorage, obtenerlo del backend
        if (!user.firstName) {
            try {
                const currentUser = await API.auth.getCurrentUser();
                localStorage.setItem('user', JSON.stringify(currentUser));
                userNameElements.forEach(el => el.textContent = currentUser.firstName);
            } catch (error) {
                console.error('Error obteniendo usuario:', error);
                // Si falla, hacer logout
                API.auth.logout();
            }
        }
    } else {
        // Usuario no autenticado
        authButtons.forEach(el => el.style.display = 'block');
        userMenus.forEach(el => el.style.display = 'none');
    }
}

// ========================================
// NOTIFICATION HELPER
// ========================================

function showNotification(message, type = 'info') {
    // Crear elemento de notificación
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;

    // Estilos inline (puedes moverlos a CSS)
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 8px;
        color: white;
        font-weight: 500;
        z-index: 10000;
        animation: slideIn 0.3s ease;
        max-width: 400px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;

    // Colores según tipo
    const colors = {
        success: '#10b981',
        error: '#ef4444',
        warning: '#f59e0b',
        info: '#3b82f6'
    };
    notification.style.background = colors[type] || colors.info;

    // Agregar al DOM
    document.body.appendChild(notification);

    // Remover después de 4 segundos
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 4000);
}

// Agregar animaciones CSS si no existen
if (!document.getElementById('notification-styles')) {
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

// ========================================
// INICIALIZACIÓN
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    checkAuthStatus();
    updateAuthUI();
    API.cart.updateCartCount();
});

// Hacer funciones disponibles globalmente
window.logout = logout;
window.showNotification = showNotification;