package it.univaq.mastereat.model;

public class UtentePasswordHash {

    private final Utente utente;
    private final String passwordHash;

    public UtentePasswordHash(Utente utente, String passwordHash) {
        this.utente = utente;
        this.passwordHash = passwordHash;
    }

    public Utente getUtente() {
        return utente;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
