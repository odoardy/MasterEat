package it.univaq.mastereat.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class TokenUtils {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TokenUtils() {
    }

    public static String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public static String extractToken(String authorizationHeader, String authenticationHeader) {
        String authorization = normalize(authorizationHeader);
        if (authorization != null
                && authorization.length() > BEARER_PREFIX.length()
                && authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return normalize(authorization.substring(BEARER_PREFIX.length()));
        }

        return normalize(authenticationHeader);
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
