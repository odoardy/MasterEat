package it.univaq.mastereat.util;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class MailSender {

    private final EmailConfig config;

    public MailSender() {
        this(EmailConfig.fromEnvironment());
    }

    public MailSender(EmailConfig config) {
        this.config = config;
    }

    public void sendPlainText(String to, String subject, String body) throws MessagingException {
        if (!config.isMailEnabled()) {
            throw new MessagingException("Invio email disabilitato da " + EmailConfig.MAIL_ENABLED_KEY);
        }

        Session session = Session.getInstance(createProperties());
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(config.getMailFrom()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setText(body, StandardCharsets.UTF_8.name());

        Transport.send(message);
    }

    private Properties createProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", config.getSmtpHost());
        properties.put("mail.smtp.port", String.valueOf(config.getSmtpPort()));
        properties.put("mail.smtp.auth", String.valueOf(config.isAuthEnabled()));
        properties.put("mail.smtp.starttls.enable", String.valueOf(config.isStartTlsEnabled()));
        properties.put("mail.smtp.connectiontimeout", String.valueOf(config.getTimeoutMs()));
        properties.put("mail.smtp.timeout", String.valueOf(config.getTimeoutMs()));
        properties.put("mail.smtp.writetimeout", String.valueOf(config.getTimeoutMs()));
        return properties;
    }
}
