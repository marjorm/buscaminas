package com.bachillerato.buscaminas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/** Servicio de contraseñas con el PasswordEncoder oficial de Spring Security. */
@Service
public class PasswordService {
    private final PasswordEncoder codificador;

    @Autowired
    public PasswordService(PasswordEncoder codificador) {
        this.codificador = codificador;
    }

    /** Constructor para las pruebas unitarias existentes. */
    public PasswordService() {
        this(new Pbkdf2PasswordEncoder("", 16, 600_000,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256));
    }

    public static byte[] generarSal() {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        return sal;
    }

    public String generarHash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        return codificador.encode(password);
    }

    public boolean verificarPassword(String password, String hashGuardado) {
        if (password == null || hashGuardado == null ||
                !hashGuardado.matches("[0-9a-fA-F]{96}")) {
            return false;
        }
        try {
            return codificador.matches(password, hashGuardado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
