// FILE: scripts/auth.js
// Propósito: Lógica de autenticación (login y registro)
// Descripción: Validación, manejo de sesiones, registro de usuarios
// Uso: Importar en login.html y register.html después de main.js

(function() {
    'use strict';

    // ===== CONFIGURACIÓN =====
    const CONFIG = {
        MIN_PASSWORD_LENGTH: 6,
        SESSION_DURATION: 7 * 24 * 60 * 60 * 1000 // 7 días en milisegundos
    };

    // ===== UTILIDADES =====

    /**
     * Validar email
     */
    function isValidEmail(email) {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return regex.test(email);
    }

    /**
     * Validar teléfono argentino
     */
    function isValidPhone(phone) {
        const regex = /^(\+?54)?[\s\-]?9?[\s\-]?\d{2,4}[\s\-]?\d{6,8}$/;
        return regex.test(phone);
    }

    /**
     * Calcular fortaleza de contraseña
     */
    function calculatePasswordStrength(password) {
        let strength = 0;
        
        if (password.length >= 6) strength += 1;
        if (password.length >= 10) strength += 1;
        if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength += 1;
        if (/\d/.test(password)) strength += 1;
        if (/[^a-zA-Z\d]/.test(password)) strength += 1;
        
        if (strength <= 2) return 'weak';
        if (strength <= 3) return 'medium';
        return 'strong';
    }

    /**
     * Mostrar error en campo
     */
    function showFieldError(fieldId, message) {
        const field = document.getElementById(fieldId);
        const errorElement = document.getElementById(`${fieldId}Error`);
        
        if (field) {
            field.classList.add('error');
        }
        
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.classList.add('show');
        }
    }

    /**
     * Limpiar error de campo
     */
    function clearFieldError(fieldId) {
        const field = document.getElementById(fieldId);
        const errorElement = document.getElementById(`${fieldId}Error`);
        
        if (field) {
            field.classList.remove('error');
        }
        
        if (errorElement) {
            errorElement.textContent = '';
            errorElement.classList.remove('show');
        }
    }

    /**
     * Mostrar notificación
     */
    function showNotification(message, type) {
        if (window.AGUARDI && window.AGUARDI.showNotification) {
            window.AGUARDI.showNotification(message, type);
        }
    }

    // ===== GESTIÓN DE USUARIOS =====

    /**
     * Obtener todos los usuarios registrados
     */
    function getAllUsers() {
        const users = localStorage.getItem('aguardi_users');
        return users ? JSON.parse(users) : [];
    }

    /**
     * Guardar usuario
     */
    function saveUser(user) {
        const users = getAllUsers();
        users.push(user);
        localStorage.setItem('aguardi_users', JSON.stringify(users));
    }

    /**
     * Buscar usuario por email
     */
    function findUserByEmail(email) {
        const users = getAllUsers();
        return users.find(u => u.email.toLowerCase() === email.toLowerCase());
    }

    /**
     * Crear sesión de usuario
     */
    function createSession(user, remember = false) {
        const session = {
            id: user.id,
            email: user.email,
            fullName: user.fullName,
            phone: user.phone,
            role: user.role || 'client',
            logged: true,
            loginDate: new Date().toISOString(),
            expiresAt: remember ? new Date(Date.now() + CONFIG.SESSION_DURATION).toISOString() : null
        };
        
        localStorage.setItem('aguardi_user', JSON.stringify(session));
        
        if (window.AGUARDI && window.AGUARDI.updateAuthUI) {
            window.AGUARDI.updateAuthUI();
        }
    }

    /**
     * Cerrar sesión
     */
    function logout() {
        localStorage.removeItem('aguardi_user');
        window.location.href = 'index.html';
    }

    /**
     * Verificar si email ya existe
     */
    function emailExists(email) {
        return findUserByEmail(email) !== undefined;
    }

    // ===== LOGIN =====

    /**
     * Inicializar página de login
     */
    function initLoginPage() {
        const loginForm = document.getElementById('loginForm');
        const togglePassword = document.getElementById('togglePassword');
        const passwordInput = document.getElementById('password');
        const emailInput = document.getElementById('email');
        const guestBtn = document.getElementById('guestBtn');
        
        if (!loginForm) return;
        
        // Toggle mostrar/ocultar contraseña
        if (togglePassword && passwordInput) {
            togglePassword.addEventListener('click', () => {
                const type = passwordInput.type === 'password' ? 'text' : 'password';
                passwordInput.type = type;
            });
        }
        
        // Limpiar errores al escribir
        if (emailInput) {
            emailInput.addEventListener('input', () => clearFieldError('email'));
        }
        
        if (passwordInput) {
            passwordInput.addEventListener('input', () => clearFieldError('password'));
        }
        
        // Continuar como invitado
        if (guestBtn) {
            guestBtn.addEventListener('click', () => {
                window.location.href = 'catalog.html';
            });
        }
        
        // Submit del formulario
        loginForm.addEventListener('submit', handleLogin);
    }

    /**
     * Manejar login
     */
    function handleLogin(e) {
        e.preventDefault();
        
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value;
        const remember = document.getElementById('remember').checked;
        
        // Limpiar errores previos
        clearFieldError('email');
        clearFieldError('password');
        
        // Validar campos
        if (!email) {
            showFieldError('email', 'El email es obligatorio');
            return;
        }
        
        if (!isValidEmail(email)) {
            showFieldError('email', 'El email no es válido');
            return;
        }
        
        if (!password) {
            showFieldError('password', 'La contraseña es obligatoria');
            return;
        }
        
        // Buscar usuario
        const user = findUserByEmail(email);
        
        if (!user) {
            showFieldError('email', 'Email no registrado');
            showNotification('No existe una cuenta con este email', 'error');
            return;
        }
        
        // Verificar contraseña (en producción usaría bcrypt)
        if (user.password !== password) {
            showFieldError('password', 'Contraseña incorrecta');
            showNotification('Contraseña incorrecta', 'error');
            return;
        }
        
        // Crear sesión
        createSession(user, remember);
        
        showNotification(`¡Bienvenido ${user.fullName}!`, 'success');
        
        // Redirigir después de un breve delay
        setTimeout(() => {
            // Verificar si hay una URL de retorno
            const urlParams = new URLSearchParams(window.location.search);
            const redirect = urlParams.get('redirect') || 'index.html';
            window.location.href = redirect;
        }, 1000);
    }

    // ===== REGISTRO =====

    /**
     * Inicializar página de registro
     */
    function initRegisterPage() {
        const registerForm = document.getElementById('registerForm');
        const togglePassword = document.getElementById('togglePassword');
        const passwordInput = document.getElementById('password');
        const confirmPasswordInput = document.getElementById('confirmPassword');
        const fullNameInput = document.getElementById('fullName');
        const emailInput = document.getElementById('email');
        const phoneInput = document.getElementById('phone');
        const termsInput = document.getElementById('terms');
        
        if (!registerForm) return;
        
        // Toggle mostrar/ocultar contraseña
        if (togglePassword && passwordInput) {
            togglePassword.addEventListener('click', () => {
                const type = passwordInput.type === 'password' ? 'text' : 'password';
                passwordInput.type = type;
                if (confirmPasswordInput) {
                    confirmPasswordInput.type = type;
                }
            });
        }
        
        // Medidor de fortaleza de contraseña
        if (passwordInput) {
            passwordInput.addEventListener('input', (e) => {
                clearFieldError('password');
                updatePasswordStrength(e.target.value);
            });
        }
        
        // Limpiar errores al escribir
        const inputs = [fullNameInput, emailInput, phoneInput, confirmPasswordInput];
        inputs.forEach(input => {
            if (input) {
                input.addEventListener('input', () => clearFieldError(input.id));
            }
        });
        
        if (termsInput) {
            termsInput.addEventListener('change', () => clearFieldError('terms'));
        }
        
        // Submit del formulario
        registerForm.addEventListener('submit', handleRegister);
    }

    /**
     * Actualizar medidor de fortaleza de contraseña
     */
    function updatePasswordStrength(password) {
        const strengthContainer = document.getElementById('passwordStrength');
        const strengthFill = strengthContainer?.querySelector('.strength-fill');
        const strengthText = strengthContainer?.querySelector('.strength-text');
        
        if (!strengthContainer || !strengthFill || !strengthText) return;
        
        if (password.length === 0) {
            strengthContainer.classList.remove('show');
            return;
        }
        
        strengthContainer.classList.add('show');
        
        const strength = calculatePasswordStrength(password);
        
        // Remover clases anteriores
        strengthFill.classList.remove('weak', 'medium', 'strong');
        
        // Aplicar nueva clase y texto
        strengthFill.classList.add(strength);
        
        const messages = {
            'weak': 'Contraseña débil',
            'medium': 'Contraseña media',
            'strong': 'Contraseña fuerte'
        };
        
        strengthText.textContent = messages[strength];
    }

    /**
     * Manejar registro
     */
    function handleRegister(e) {
        e.preventDefault();
        
        const fullName = document.getElementById('fullName').value.trim();
        const email = document.getElementById('email').value.trim();
        const phone = document.getElementById('phone').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const terms = document.getElementById('terms').checked;
        
        // Limpiar errores previos
        ['fullName', 'email', 'phone', 'password', 'confirmPassword', 'terms'].forEach(clearFieldError);
        
        let hasErrors = false;
        
        // Validar nombre completo
        if (!fullName) {
            showFieldError('fullName', 'El nombre es obligatorio');
            hasErrors = true;
        } else if (fullName.length < 3) {
            showFieldError('fullName', 'El nombre debe tener al menos 3 caracteres');
            hasErrors = true;
        }
        
        // Validar email
        if (!email) {
            showFieldError('email', 'El email es obligatorio');
            hasErrors = true;
        } else if (!isValidEmail(email)) {
            showFieldError('email', 'El email no es válido');
            hasErrors = true;
        } else if (emailExists(email)) {
            showFieldError('email', 'Este email ya está registrado');
            hasErrors = true;
        }
        
        // Validar teléfono
        if (!phone) {
            showFieldError('phone', 'El teléfono es obligatorio');
            hasErrors = true;
        } else if (!isValidPhone(phone)) {
            showFieldError('phone', 'El teléfono no es válido');
            hasErrors = true;
        }
        
        // Validar contraseña
        if (!password) {
            showFieldError('password', 'La contraseña es obligatoria');
            hasErrors = true;
        } else if (password.length < CONFIG.MIN_PASSWORD_LENGTH) {
            showFieldError('password', `La contraseña debe tener al menos ${CONFIG.MIN_PASSWORD_LENGTH} caracteres`);
            hasErrors = true;
        }
        
        // Validar confirmación de contraseña
        if (!confirmPassword) {
            showFieldError('confirmPassword', 'Confirma tu contraseña');
            hasErrors = true;
        } else if (password !== confirmPassword) {
            showFieldError('confirmPassword', 'Las contraseñas no coinciden');
            hasErrors = true;
        }
        
        // Validar términos
        if (!terms) {
            showFieldError('terms', 'Debes aceptar los términos y condiciones');
            hasErrors = true;
        }
        
        if (hasErrors) {
            showNotification('Por favor corrige los errores del formulario', 'error');
            return;
        }
        
        // Crear nuevo usuario
        const newUser = {
            id: Date.now(),
            fullName,
            email,
            phone,
            password, // En producción: hashear con bcrypt
            role: 'client',
            createdAt: new Date().toISOString()
        };
        
        // Guardar usuario
        saveUser(newUser);
        
        // Crear sesión automáticamente
        createSession(newUser, false);
        
        showNotification('¡Cuenta creada exitosamente!', 'success');
        
        // Redirigir después de un breve delay
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 1500);
    }

    // ===== INICIALIZACIÓN =====

    /**
     * Inicializar según la página actual
     */
    function init() {
        // Detectar página actual
        const path = window.location.pathname;
        
        if (path.includes('login.html')) {
            initLoginPage();
        } else if (path.includes('register.html')) {
            initRegisterPage();
        }
        
        console.log('Sistema de autenticación AGUARDI inicializado');
    }

    // Inicializar cuando el DOM esté listo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // ===== EXPORTAR FUNCIONES GLOBALES =====
    window.AGUARDI = window.AGUARDI || {};
    window.AGUARDI.logout = logout;

})();