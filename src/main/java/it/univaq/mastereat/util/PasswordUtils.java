package it.univaq.mastereat.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtils {

    private PasswordUtils() {
    }

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexadecimal = new StringBuilder(hash.length * 2);

            for (byte value : hash) {
                hexadecimal.append(Character.forDigit((value >> 4) & 0xF, 16));
                hexadecimal.append(Character.forDigit(value & 0xF, 16));
            }

            return hexadecimal.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Algoritmo SHA-256 non disponibile", exception);
        }
    }
}
