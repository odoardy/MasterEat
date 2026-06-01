package it.univaq.mastereat.service;

import it.univaq.mastereat.dao.UtenteDAO;
import it.univaq.mastereat.dao.impl.UtenteDAOImpl;
import it.univaq.mastereat.dto.web.customer.ClienteProfileUpdateRequest;
import it.univaq.mastereat.dto.web.auth.ClienteRegistrationRequest;
import it.univaq.mastereat.dto.web.owner.OwnerPersonaleCreateRequest;
import it.univaq.mastereat.dto.web.owner.OwnerPersonaleResponse;
import it.univaq.mastereat.model.Utente;
import it.univaq.mastereat.util.PasswordHasher;
import it.univaq.mastereat.util.PasswordPolicy;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class UtenteService {

    private static final String RUOLO_CLIENTE = "CLIENTE";
    private static final String RUOLO_PERSONALE = "PERSONALE";
    private static final String RUOLO_PROPRIETARIO = "PROPRIETARIO";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+0-9 .()\\-]{6,30}$");

    private final UtenteDAO utenteDAO;

    public UtenteService() {
        this.utenteDAO = new UtenteDAOImpl();
    }

    public Utente registraCliente(ClienteRegistrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dati di registrazione non validi.");
        }

        String username = normalizeRequired(request.getUsername(), "Username");
        String email = normalizeRequired(request.getEmail(), "Email").toLowerCase(Locale.ROOT);
        String password = requirePassword(request.getPassword());
        String nome = normalizeRequired(request.getNome(), "Nome");
        String cognome = normalizeRequired(request.getCognome(), "Cognome");
        String telefono = normalizeRequired(request.getTelefono(), "Telefono");
        String indirizzo = normalizeRequired(request.getIndirizzo(), "Indirizzo");
        String citta = normalizeRequired(request.getCitta(), "Citta");
        String cap = normalizeOptional(request.getCap());

        validatePassword(password);

        try {
            if (utenteDAO.existsByUsername(username)) {
                throw new IllegalArgumentException("Username gia esistente. Scegline un altro.");
            }

            if (utenteDAO.existsByEmail(email)) {
                throw new IllegalArgumentException("Email gia registrata. Usa un altro indirizzo email.");
            }

            String passwordHash = PasswordHasher.hash(password);
            return utenteDAO.createCliente(
                    username,
                    email,
                    passwordHash,
                    nome,
                    cognome,
                    telefono,
                    indirizzo,
                    citta,
                    cap
            );
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new IllegalArgumentException("Username o email gia registrati.");
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la registrazione del cliente", exception);
        }
    }

    public Optional<Utente> getUtenteById(long idUtente) {
        if (idUtente <= 0) {
            throw new IllegalArgumentException("Id utente non valido.");
        }

        try {
            return utenteDAO.findById(Math.toIntExact(idUtente));
        } catch (ArithmeticException exception) {
            return Optional.empty();
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dell'utente", exception);
        }
    }

    public List<OwnerPersonaleResponse> getPersonaleProprietario(long idProprietario) {
        Utente proprietario = requireUtenteAutenticato(idProprietario);
        requireProprietario(proprietario);

        try {
            return toOwnerPersonaleResponse(utenteDAO.findByRole(RUOLO_PERSONALE));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero del personale", exception);
        }
    }

    public OwnerPersonaleResponse registraPersonale(long idProprietario, OwnerPersonaleCreateRequest request) {
        Utente proprietario = requireUtenteAutenticato(idProprietario);
        requireProprietario(proprietario);

        if (request == null) {
            throw new IllegalArgumentException("Dati personale non validi.");
        }

        String username = normalizeRequired(request.getUsername(), "Username");
        String email = normalizeRequired(request.getEmail(), "Email").toLowerCase(Locale.ROOT);
        String password = requirePassword(request.getPassword());
        String confermaPassword = requirePasswordConfirm(request.getConfermaPassword());
        String nome = normalizeRequired(request.getNome(), "Nome");
        String cognome = normalizeRequired(request.getCognome(), "Cognome");
        String telefono = normalizeOptional(request.getTelefono());

        validateLength(username, "Username", 50);
        validateLength(email, "Email", 255);
        validateLength(nome, "Nome", 80);
        validateLength(cognome, "Cognome", 80);
        validateLength(telefono, "Telefono", 30);
        if (username.length() < 3) {
            throw new IllegalArgumentException("Username deve contenere almeno 3 caratteri.");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username non valido: usa solo lettere, numeri, punto, trattino o underscore.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email non valida.");
        }
        if (telefono != null && !PHONE_PATTERN.matcher(telefono).matches()) {
            throw new IllegalArgumentException("Telefono non valido.");
        }
        validatePassword(password);
        if (!Objects.equals(password, confermaPassword)) {
            throw new IllegalArgumentException("Password e conferma password non coincidono.");
        }

        try {
            if (utenteDAO.existsByUsername(username)) {
                throw new IllegalArgumentException("Username gia esistente. Scegline un altro.");
            }
            if (utenteDAO.existsByEmail(email)) {
                throw new IllegalArgumentException("Email gia registrata. Usa un altro indirizzo email.");
            }

            String passwordHash = PasswordHasher.hash(password);
            return toOwnerPersonaleResponse(utenteDAO.createPersonale(
                    username,
                    email,
                    passwordHash,
                    nome,
                    cognome,
                    telefono
            ));
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new IllegalArgumentException("Username o email gia registrati.");
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la registrazione del personale", exception);
        }
    }

    public Utente aggiornaProfiloCliente(long idUtente, ClienteProfileUpdateRequest request) {
        if (idUtente <= 0) {
            throw new IllegalArgumentException("Id utente non valido.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Dati profilo non validi.");
        }

        String nome = normalizeRequired(request.getNome(), "Nome");
        String cognome = normalizeRequired(request.getCognome(), "Cognome");
        String email = normalizeRequired(request.getEmail(), "Email").toLowerCase(Locale.ROOT);
        String telefono = normalizeRequired(request.getTelefono(), "Telefono");
        String indirizzo = normalizeRequired(request.getIndirizzo(), "Indirizzo");
        String citta = normalizeRequired(request.getCitta(), "Citta");
        String cap = normalizeOptional(request.getCap());

        validateLength(nome, "Nome", 80);
        validateLength(cognome, "Cognome", 80);
        validateLength(email, "Email", 255);
        validateLength(telefono, "Telefono", 30);
        validateLength(indirizzo, "Indirizzo", 255);
        validateLength(citta, "Citta", 100);
        validateLength(cap, "CAP", 20);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email non valida.");
        }

        try {
            Utente utente = utenteDAO.findById(Math.toIntExact(idUtente))
                    .orElseThrow(() -> new NoSuchElementException("Cliente non trovato."));
            if (!RUOLO_CLIENTE.equals(utente.getRuolo())) {
                throw new SecurityException("Operazione consentita solo a utenti CLIENTE.");
            }

            if (utenteDAO.existsByEmailForOtherUser(email, idUtente)) {
                throw new IllegalArgumentException("Email gia usata da un altro utente.");
            }

            return utenteDAO.updateClienteProfile(
                    idUtente,
                    nome,
                    cognome,
                    email,
                    telefono,
                    indirizzo,
                    citta,
                    cap
            ).orElseThrow(() -> new NoSuchElementException("Cliente non trovato."));
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new IllegalArgumentException("Email gia usata da un altro utente.");
        } catch (ArithmeticException exception) {
            throw new NoSuchElementException("Cliente non trovato.");
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante l'aggiornamento del profilo cliente", exception);
        }
    }

    private String normalizeRequired(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " obbligatorio.");
        }
        return value.trim();
    }

    private String requirePassword(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password obbligatoria.");
        }
        return value;
    }

    private String requirePasswordConfirm(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Conferma password obbligatoria.");
        }
        return value;
    }

    private void validatePassword(String value) {
        if (value.length() > 128) {
            throw new IllegalArgumentException("Password deve contenere al massimo 128 caratteri.");
        }
        if (!PasswordPolicy.isValidNewPassword(value)) {
            throw new IllegalArgumentException(PasswordPolicy.ERROR_MESSAGE);
        }
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void validateLength(String value, String label, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(label + " deve contenere al massimo " + maxLength + " caratteri.");
        }
    }

    private Utente requireUtenteAutenticato(long idUtente) {
        if (idUtente <= 0) {
            throw new SecurityException("Utente non autorizzato.");
        }

        try {
            return utenteDAO.findById(Math.toIntExact(idUtente))
                    .orElseThrow(() -> new SecurityException("Utente non autorizzato."));
        } catch (ArithmeticException exception) {
            throw new SecurityException("Utente non autorizzato.");
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dell'utente", exception);
        }
    }

    private void requireProprietario(Utente utente) {
        if (RUOLO_PROPRIETARIO.equals(utente.getRuolo())) {
            return;
        }

        throw new SecurityException("Operazione consentita solo a PROPRIETARIO.");
    }

    private List<OwnerPersonaleResponse> toOwnerPersonaleResponse(List<Utente> utenti) {
        if (utenti == null || utenti.isEmpty()) {
            return Collections.emptyList();
        }

        List<OwnerPersonaleResponse> response = new ArrayList<>();
        for (Utente utente : utenti) {
            response.add(toOwnerPersonaleResponse(utente));
        }
        return response;
    }

    private OwnerPersonaleResponse toOwnerPersonaleResponse(Utente utente) {
        return new OwnerPersonaleResponse(
                utente.getId(),
                utente.getUsername(),
                utente.getNome(),
                utente.getCognome(),
                utente.getEmail(),
                utente.getTelefono(),
                utente.getCreatoIl()
        );
    }
}
