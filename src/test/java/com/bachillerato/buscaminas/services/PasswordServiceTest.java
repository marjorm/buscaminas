package com.bachillerato.buscaminas.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {
    private final PasswordService servicio = new PasswordService();

    @Test
    void debeGenerarHashDe96Caracteres() {
        assertEquals(96, servicio.generarHash("Clave123").length());
    }

    @Test
    void debeAceptarPasswordCorrecto() {
        String hash = servicio.generarHash("Clave123");
        assertTrue(servicio.verificarPassword("Clave123", hash));
    }

    @Test
    void debeRechazarPasswordIncorrecto() {
        String hash = servicio.generarHash("Clave123");
        assertFalse(servicio.verificarPassword("ClaveIncorrecta", hash));
    }

    @Test
    void debeAceptarLosUsuariosDePruebaDelEnunciado() {
        assertTrue(servicio.verificarPassword("Prueba123$",
                "7a4b794392da7ee15963ef339a2af0332757b9c8a4485767af1579af9c5231f792c87483001d0872b9ee0a18fa722df4"));
        assertTrue(servicio.verificarPassword("Prueba123$",
                "54c1f0100d3f5c01e877d95270b7b4e8e2a7d46f241fc0410ce80693c8519fc10c4f4ca8f7cf7438256bc54a8e352055"));
    }

    @Test
    void debeRechazarFormatoHashInvalido() {
        assertFalse(servicio.verificarPassword("Clave123", "123"));
        assertFalse(servicio.verificarPassword("Clave123", null));
    }
}
