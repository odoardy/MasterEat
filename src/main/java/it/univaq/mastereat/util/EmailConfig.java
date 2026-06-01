package it.univaq.mastereat.util;

/**
 * Configurazione SMTP letta da system property o variabili d'ambiente.
 *
 * I valori predefiniti puntano al server locale usato in sviluppo con
 * FakeSMTP, mantenendo l'invio email disattivabile senza cambiare codice.
 * 
 * Per disabilitare l'invio email, basta impostare MAIL_ENABLED_KEY a false.
 */
public final class EmailConfig {

    public static final String SMTP_HOST_KEY = "MASTEREAT_SMTP_HOST";
    public static final String SMTP_PORT_KEY = "MASTEREAT_SMTP_PORT";
    public static final String MAIL_FROM_KEY = "MASTEREAT_MAIL_FROM";
    public static final String MAIL_ENABLED_KEY = "MASTEREAT_MAIL_ENABLED";

    private static final String DEFAULT_SMTP_HOST = "localhost";
    private static final int DEFAULT_SMTP_PORT = 2525;
    private static final String DEFAULT_MAIL_FROM = "noreply@mastereat.local";
    private static final int DEFAULT_TIMEOUT_MS = 2000;

    private final String smtpHost;
    private final int smtpPort;
    private final String mailFrom;
    private final boolean mailEnabled;
    private final int timeoutMs;

    private EmailConfig(String smtpHost,
                        int smtpPort,
                        String mailFrom,
                        boolean mailEnabled,
                        int timeoutMs) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.mailFrom = mailFrom;
        this.mailEnabled = mailEnabled;
        this.timeoutMs = timeoutMs;
    }

    public static EmailConfig fromEnvironment() {
        return new EmailConfig(
                readString(SMTP_HOST_KEY, DEFAULT_SMTP_HOST),
                readPort(SMTP_PORT_KEY, DEFAULT_SMTP_PORT),
                readString(MAIL_FROM_KEY, DEFAULT_MAIL_FROM),
                readBoolean(MAIL_ENABLED_KEY, true),
                DEFAULT_TIMEOUT_MS
        );
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public boolean isMailEnabled() {
        return mailEnabled;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public boolean isAuthEnabled() {
        return false;
    }

    public boolean isStartTlsEnabled() {
        return false;
    }

    private static String readString(String key, String defaultValue) {
        String value = readOverride(key);
        return value != null ? value : defaultValue;
    }

    private static int readPort(String key, int defaultValue) {
        String value = readOverride(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            int port = Integer.parseInt(value);
            if (port > 0 && port <= 65535) {
                return port;
            }
        } catch (NumberFormatException ignored) {
            // Default locale SMTP di test.
        }

        return defaultValue;
    }

    private static boolean readBoolean(String key, boolean defaultValue) {
        String value = readOverride(key);
        if (value == null) {
            return defaultValue;
        }

        return switch (value.toLowerCase()) {
            case "true", "1", "yes", "y", "on" -> true;
            case "false", "0", "no", "n", "off" -> false;
            default -> defaultValue;
        };
    }

    private static String readOverride(String key) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        String environmentVariable = System.getenv(key);
        if (environmentVariable != null && !environmentVariable.isBlank()) {
            return environmentVariable.trim();
        }

        return null;
    }
}
