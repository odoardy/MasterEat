package it.univaq.mastereat.service;

import it.univaq.mastereat.dao.NotificaEmailDAO;
import it.univaq.mastereat.dao.OrdineDAO;
import it.univaq.mastereat.dao.UtenteDAO;
import it.univaq.mastereat.dao.impl.NotificaEmailDAOImpl;
import it.univaq.mastereat.dao.impl.OrdineDAOImpl;
import it.univaq.mastereat.dao.impl.UtenteDAOImpl;
import it.univaq.mastereat.model.CaratteristicaRigaOrdine;
import it.univaq.mastereat.model.NotificaEmail;
import it.univaq.mastereat.model.Ordine;
import it.univaq.mastereat.model.RigaOrdine;
import it.univaq.mastereat.model.Utente;
import it.univaq.mastereat.util.MailSender;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service per le notifiche email legate al ciclo dell'ordine.
 *
 * Registra lo stato della notifica a database e tenta l'invio tramite SMTP
 * locale/FakeSMTP, ma gestisce ogni errore in modalita best-effort per non
 * interrompere il checkout o le transizioni ordine.
 */
public class EmailNotificationService {

    private static final Logger LOGGER = Logger.getLogger(EmailNotificationService.class.getName());
    private static final String EMAIL_MANCANTE = "EMAIL_MANCANTE";
    private static final DateTimeFormatter FORMATO_ORARIO_EMAIL =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificaEmailDAO notificaEmailDAO;
    private final OrdineDAO ordineDAO;
    private final UtenteDAO utenteDAO;
    private final MailSender mailSender;

    public EmailNotificationService() {
        this(
                new NotificaEmailDAOImpl(),
                new OrdineDAOImpl(),
                new UtenteDAOImpl(),
                new MailSender()
        );
    }

    EmailNotificationService(NotificaEmailDAO notificaEmailDAO,
                             OrdineDAO ordineDAO,
                             UtenteDAO utenteDAO,
                             MailSender mailSender) {
        this.notificaEmailDAO = notificaEmailDAO;
        this.ordineDAO = ordineDAO;
        this.utenteDAO = utenteDAO;
        this.mailSender = mailSender;
    }

    public void notificaOrdineConfermato(long idOrdine) {
        notificaOrdine(idOrdine, NotificaEmail.Tipo.ORDINE_CONFERMATO);
    }

    public void notificaOrdineInConsegna(long idOrdine) {
        notificaOrdine(idOrdine, NotificaEmail.Tipo.ORDINE_IN_CONSEGNA);
    }

    /**
     * Crea la notifica se non gia presente e prova l'invio. Qualsiasi errore
     * viene loggato e, quando possibile, salvato come stato FALLITA.
     */
    private void notificaOrdine(long idOrdine, NotificaEmail.Tipo tipo) {
        try {
            if (idOrdine <= 0) {
                LOGGER.warning("Notifica email non inviata: id ordine non valido " + idOrdine);
                return;
            }

            if (notificaEmailDAO.existsDaInviareOInviata(idOrdine, tipo)) {
                LOGGER.info("Notifica email " + tipo.name() + " gia presente per ordine #" + idOrdine);
                return;
            }

            Optional<Ordine> ordineOptional = ordineDAO.findById(idOrdine);
            if (ordineOptional.isEmpty()) {
                LOGGER.warning("Notifica email " + tipo.name() + " non inviata: ordine #" + idOrdine + " non trovato");
                return;
            }

            Ordine ordine = ordineOptional.get();
            Optional<Utente> cliente = findClienteSafe(ordine.getIdCliente());
            String emailDestinatario = cliente.map(Utente::getEmail)
                    .map(String::trim)
                    .filter(email -> !email.isBlank())
                    .orElse(null);

            String oggetto = buildOggetto(tipo, ordine.getId());
            String corpo = buildCorpo(tipo, ordine, cliente);
            NotificaEmail notifica = notificaEmailDAO.createDaInviare(
                    ordine.getId(),
                    emailDestinatario != null ? emailDestinatario : EMAIL_MANCANTE,
                    tipo,
                    oggetto
            );

            if (emailDestinatario == null) {
                String messaggio = "Email cliente mancante per ordine #" + ordine.getId();
                LOGGER.warning(messaggio);
                marcaFallitaSafe(notifica.getId(), messaggio);
                return;
            }

            try {
                mailSender.sendPlainText(emailDestinatario, oggetto, corpo);
                notificaEmailDAO.marcaInviata(notifica.getId());
                LOGGER.info("Notifica email " + tipo.name() + " inviata per ordine #" + ordine.getId());
            } catch (Exception exception) {
                LOGGER.log(
                        Level.WARNING,
                        "Invio email " + tipo.name() + " fallito per ordine #" + ordine.getId(),
                        exception
                );
                marcaFallitaSafe(notifica.getId(), getMessaggioErrore(exception));
            }
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "Gestione notifica email " + tipo.name()
                    + " fallita per ordine #" + idOrdine, exception);
        }
    }

    private Optional<Utente> findClienteSafe(long idCliente) throws SQLException {
        try {
            return utenteDAO.findById(Math.toIntExact(idCliente));
        } catch (ArithmeticException exception) {
            return Optional.empty();
        }
    }

    private String buildOggetto(NotificaEmail.Tipo tipo, long idOrdine) {
        return switch (tipo) {
            case ORDINE_CONFERMATO -> "MasterEat - Conferma ordine #" + idOrdine;
            case ORDINE_IN_CONSEGNA -> "MasterEat - Ordine #" + idOrdine + " in consegna";
        };
    }

    private String buildCorpo(NotificaEmail.Tipo tipo,
                              Ordine ordine,
                              Optional<Utente> cliente) throws SQLException {
        return switch (tipo) {
            case ORDINE_CONFERMATO -> buildCorpoOrdineConfermato(ordine, cliente);
            case ORDINE_IN_CONSEGNA -> buildCorpoOrdineInConsegna(ordine, cliente);
        };
    }

    private String buildCorpoOrdineConfermato(Ordine ordine, Optional<Utente> cliente) throws SQLException {
        StringBuilder body = new StringBuilder();
        body.append("Ciao ").append(formatNomeCliente(cliente, ordine.getIdCliente())).append(",\n");
        body.append("il tuo ordine #").append(ordine.getId()).append(" è stato confermato.\n\n");
        body.append("Totale: ").append(formatImporto(ordine.getPrezzoTotale())).append(" €\n");

        if (!isBlank(ordine.getOrarioConsegnaRichiesto())) {
            body.append("Orario richiesto: ")
                    .append(formatOrario(ordine.getOrarioConsegnaRichiesto()))
                    .append("\n");
        }

        body.append("\nProdotti:\n");
        List<RigaOrdine> righe = ordineDAO.findRigheByOrdineId(ordine.getId());
        if (righe.isEmpty()) {
            body.append("- Nessun prodotto disponibile\n");
        } else {
            for (RigaOrdine riga : righe) {
                body.append("- ")
                        .append(riga.getQuantita())
                        .append("x ")
                        .append(riga.getNomeProdottoSnapshot());

                String caratteristiche = formatCaratteristiche(riga.getCaratteristiche());
                if (!caratteristiche.isBlank()) {
                    body.append(" (").append(caratteristiche).append(")");
                }
                body.append("\n");
            }
        }

        body.append("\nGrazie per aver ordinato con MasterEat.");
        return body.toString();
    }

    private String buildCorpoOrdineInConsegna(Ordine ordine, Optional<Utente> cliente) {
        StringBuilder body = new StringBuilder();
        body.append("Ciao ").append(formatNomeCliente(cliente, ordine.getIdCliente())).append(",\n");
        body.append("il tuo ordine #").append(ordine.getId()).append(" è ora in consegna.\n\n");

        String indirizzo = formatIndirizzo(ordine);
        if (!indirizzo.isBlank()) {
            body.append("Indirizzo: ").append(indirizzo).append("\n\n");
        }

        body.append("Grazie per aver ordinato con MasterEat.");
        return body.toString();
    }

    private String formatNomeCliente(Optional<Utente> cliente, long idCliente) {
        if (cliente.isEmpty()) {
            return "Cliente #" + idCliente;
        }

        String nomeCompleto = ((cliente.get().getNome() != null ? cliente.get().getNome() : "") + " "
                + (cliente.get().getCognome() != null ? cliente.get().getCognome() : "")).trim();
        if (!nomeCompleto.isBlank()) {
            return nomeCompleto;
        }
        if (!isBlank(cliente.get().getUsername())) {
            return cliente.get().getUsername();
        }

        return "Cliente #" + idCliente;
    }

    private String formatImporto(BigDecimal importo) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ITALY);
        formatter.setGroupingUsed(false);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(importo != null ? importo : BigDecimal.ZERO);
    }

    private String formatOrario(String value) {
        try {
            return FORMATO_ORARIO_EMAIL.format(LocalDateTime.parse(value));
        } catch (DateTimeParseException exception) {
            return value;
        }
    }

    private String formatCaratteristiche(List<CaratteristicaRigaOrdine> caratteristiche) {
        if (caratteristiche == null || caratteristiche.isEmpty()) {
            return "";
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (CaratteristicaRigaOrdine caratteristica : caratteristiche) {
            if (!isBlank(caratteristica.getNomeCaratteristicaSnapshot())) {
                joiner.add(caratteristica.getNomeCaratteristicaSnapshot());
            }
        }
        return joiner.toString();
    }

    private String formatIndirizzo(Ordine ordine) {
        List<String> parti = new ArrayList<>();
        if (!isBlank(ordine.getIndirizzoConsegnaSnapshot())) {
            parti.add(ordine.getIndirizzoConsegnaSnapshot());
        }

        String capCitta = formatCapCitta(ordine.getCapConsegnaSnapshot(), ordine.getCittaConsegnaSnapshot());
        if (!capCitta.isBlank()) {
            parti.add(capCitta);
        }

        return String.join(", ", parti);
    }

    private String formatCapCitta(String cap, String citta) {
        StringBuilder builder = new StringBuilder();
        if (!isBlank(cap)) {
            builder.append(cap);
        }
        if (!isBlank(citta)) {
            if (!builder.isEmpty()) {
                builder.append(" ");
            }
            builder.append(citta);
        }
        return builder.toString();
    }

    /**
     * Aggiorna lo stato fallito senza propagare errori secondari del database.
     */
    private void marcaFallitaSafe(long idNotifica, String messaggioErrore) {
        try {
            notificaEmailDAO.marcaFallita(idNotifica, messaggioErrore);
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "Aggiornamento notifica email FALLITA non riuscito #" + idNotifica, exception);
        }
    }

    private String getMessaggioErrore(Exception exception) {
        String message = exception.getMessage();
        if (!isBlank(message)) {
            return exception.getClass().getSimpleName() + ": " + message;
        }
        return exception.getClass().getSimpleName();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
