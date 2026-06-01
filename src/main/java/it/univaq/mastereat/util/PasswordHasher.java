package it.univaq.mastereat.util;

import org.mindrot.jbcrypt.BCrypt;

import java.util.regex.Pattern;

public final class PasswordHasher {

    private static final int BCRYPT_COST = 12;
    private static final Pattern SHA256_HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]{64}$");

    private PasswordHasher() {
    }

    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password obbligatoria.");
        }

        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
    }

    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }

        if (isLegacySha256Hash(storedHash)) {
            return PasswordUtils.sha256(password).equalsIgnoreCase(storedHash);
        }

        try {
            return BCrypt.checkpw(password, storedHash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static boolean isLegacySha256Hash(String storedHash) {
        return storedHash != null && SHA256_HEX_PATTERN.matcher(storedHash).matches();
    }
}
