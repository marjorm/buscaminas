package com.bachillerato.buscaminas.services;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

@Service
public class PasswordService {

    private static final int ITERACIONES_PBKDF2 = 600_000;
    private static final int TAMANO_SAL = 16;
    private static final int TAMANO_HASH_BITS = 256;

    public static byte[] generarSal() {
        SecureRandom random = new SecureRandom();
        byte[] sal = new byte[TAMANO_SAL];
        random.nextBytes(sal);
        return sal;
    }

    public String generarHash(String password) {
        PBEKeySpec spec = null;

        try {

            byte[] sal = generarSal();
            spec = new PBEKeySpec(
                    password.toCharArray(),
                    sal,
                    ITERACIONES_PBKDF2,
                    TAMANO_HASH_BITS
            );


            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = fabrica.generateSecret(spec).getEncoded();


            String salHex = HexFormat.of().formatHex(sal);
            String hashHex = HexFormat.of().formatHex(hash);

            return salHex + hashHex;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error al generar el hash PBKDF2", e
            );

        } finally {
            if (spec != null) {
                spec.clearPassword();
            }
        }
    }

    public boolean verificarPassword(String password, String hashGuardado) {
        PBEKeySpec spec = null;

        try {
            if (password == null || hashGuardado == null || hashGuardado.length() != 96) {
                return false;
            }


            String salHex = hashGuardado.substring(0, 32);
            String hashHex = hashGuardado.substring(32);

            byte[] sal = HexFormat.of().parseHex(salHex);
            byte[] hashEsperado = HexFormat.of().parseHex(hashHex);

            spec = new PBEKeySpec(
                    password.toCharArray(),
                    sal,
                    ITERACIONES_PBKDF2,
                    TAMANO_HASH_BITS
            );

            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hashCalculado = fabrica.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(hashEsperado, hashCalculado);

        } catch (Exception e) {
            return false;

        } finally {
            if (spec != null) {
                spec.clearPassword();
            }
        }
    }

}