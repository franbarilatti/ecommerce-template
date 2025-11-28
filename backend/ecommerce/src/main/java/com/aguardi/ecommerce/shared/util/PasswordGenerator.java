// ============================================
// FILE: src/main/java/com/aguardi/shared/util/PasswordGenerator.java
// Propósito: Utilidad para generar y validar passwords con BCrypt
// ============================================

package com.aguardi.ecommerce.shared.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad para generar hashes de passwords
 * Útil para crear usuarios iniciales o testing
 */
public class PasswordGenerator {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private PasswordGenerator() {
        // Utility class
    }

    /**
     * Generar hash BCrypt de un password
     * @param rawPassword Password en texto plano
     * @return Hash BCrypt
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * Verificar si un password coincide con su hash
     * @param rawPassword Password en texto plano
     * @param encodedPassword Hash BCrypt
     * @return true si coinciden, false si no
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Método main para generar passwords desde línea de comandos
     * Uso: java PasswordGenerator "MiPassword123"
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java PasswordGenerator <password>");
            System.out.println("Ejemplo: java PasswordGenerator Admin123!");
            return;
        }

        String password = args[0];
        String encoded = encode(password);

        System.out.println("========================================");
        System.out.println("Password Hash Generator");
        System.out.println("========================================");
        System.out.println("Password: " + password);
        System.out.println("Hash BCrypt: " + encoded);
        System.out.println("========================================");
        System.out.println("Verificación: " + matches(password, encoded));
    }
}