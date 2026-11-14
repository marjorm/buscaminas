package com.bachillerato.buscaminas.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {
    @Test
    void debeGenerarHashDe96Caracteres() {
        // Crear una instancia del servicio
        PasswordService passwordService = new PasswordService();

        // Generar un hash de prueba
        String hash = passwordService.generarHash("Clave123");

        // Comprobar que tenga exactamente 96 caracteres
        assertEquals(96, hash.length());
    }

    @Test
    void debeAceptarPasswordCorrecto() {
        PasswordService passwordService = new PasswordService();

        String hash = passwordService.generarHash("Clave123");

        assertTrue(passwordService.verificarPassword("Clave123", hash));
    }
    @Test
    void debeRechazarPasswordIncorrecto() {
        PasswordService passwordService = new PasswordService();

        String hash = passwordService.generarHash("Clave123");

        assertFalse(passwordService.verificarPassword("ClaveIncorrecta", hash));
    }

}