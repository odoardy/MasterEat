package it.univaq.mastereat.dto.web.auth;

import it.univaq.mastereat.model.Utente;

import java.io.Serializable;

public class WebUserSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long idUtente;
    private final String username;
    private final String ruolo;
    private final String nome;
    private final String cognome;

    public WebUserSession(long idUtente,
                          String username,
                          String ruolo,
                          String nome,
                          String cognome) {
        this.idUtente = idUtente;
        this.username = username;
        this.ruolo = ruolo;
        this.nome = nome;
        this.cognome = cognome;
    }

    public static WebUserSession fromUtente(Utente utente) {
        return new WebUserSession(
                utente.getId(),
                utente.getUsername(),
                utente.getRuolo(),
                utente.getNome(),
                utente.getCognome()
        );
    }

    public long getIdUtente() {
        return idUtente;
    }

    public String getUsername() {
        return username;
    }

    public String getRuolo() {
        return ruolo;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }
}
