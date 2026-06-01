package it.univaq.mastereat.service;

import it.univaq.mastereat.dao.SessioneApiDAO;
import it.univaq.mastereat.dao.UtenteDAO;
import it.univaq.mastereat.dao.impl.SessioneApiDAOImpl;
import it.univaq.mastereat.dao.impl.UtenteDAOImpl;
import it.univaq.mastereat.dto.api.auth.LoginRequest;
import it.univaq.mastereat.dto.api.auth.LoginResponse;
import it.univaq.mastereat.model.SessioneApi;
import it.univaq.mastereat.model.Utente;
import it.univaq.mastereat.model.UtentePasswordHash;
import it.univaq.mastereat.util.PasswordHasher;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UtenteDAO utenteDAO;
    private final SessioneApiDAO sessioneApiDAO;

    public AuthService() {
        this.utenteDAO = new UtenteDAOImpl();
        this.sessioneApiDAO = new SessioneApiDAOImpl();
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Username e password sono obbligatori");
        }

        try {
            Optional<Utente> utente = autenticaUtente(request.getUsername(), request.getPassword());

            if (utente.isEmpty()) {
                return null;
            }

            Utente utenteAutenticato = utente.get();
            String token = sessioneApiDAO.createSession(Math.toIntExact(utenteAutenticato.getId()));

            return new LoginResponse(
                    token,
                    utenteAutenticato.getId(),
                    utenteAutenticato.getUsername(),
                    utenteAutenticato.getRuolo()
            );
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il login", exception);
        }
    }

    public Optional<Utente> autenticaUtente(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new IllegalArgumentException("Username e password sono obbligatori");
        }

        try {
            Optional<UtentePasswordHash> credentials =
                    utenteDAO.findActiveByUsernameWithPasswordHash(username.trim());
            if (credentials.isEmpty()) {
                return Optional.empty();
            }

            UtentePasswordHash utentePasswordHash = credentials.get();
            String storedHash = utentePasswordHash.getPasswordHash();
            if (!PasswordHasher.verify(password, storedHash)) {
                return Optional.empty();
            }

            if (PasswordHasher.isLegacySha256Hash(storedHash)) {
                utenteDAO.updatePasswordHash(
                        utentePasswordHash.getUtente().getId(),
                        PasswordHasher.hash(password)
                );
            }

            return Optional.of(utentePasswordHash.getUtente());
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante l'autenticazione", exception);
        }
    }

    public boolean logout(String token) {
        try {
            return sessioneApiDAO.invalidateSession(token);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il logout", exception);
        }
    }

    public Optional<SessioneApi> verificaToken(String token) {
        try {
            return sessioneApiDAO.findByToken(token);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la verifica del token", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
