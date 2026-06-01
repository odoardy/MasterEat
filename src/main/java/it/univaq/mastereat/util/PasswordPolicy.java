package it.univaq.mastereat.util;

import java.util.regex.Pattern;

public final class PasswordPolicy {

    public static final String ERROR_MESSAGE =
            "Password deve contenere almeno 8 caratteri, una lettera maiuscola, una lettera minuscola e un numero.";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private PasswordPolicy() {
    }

    public static boolean isValidNewPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
