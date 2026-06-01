package it.univaq.mastereat.dto.api.ordini;

import java.util.ArrayList;
import java.util.List;

public class OperatoreOrdineResponse {

    private long idUtente;
    private String username;
    private String nome;
    private String cognome;
    private String ruolo;
    private List<CambioStatoOperatoreResponse> cambiStato = new ArrayList<>();

    public OperatoreOrdineResponse() {
    }

    public OperatoreOrdineResponse(long idUtente,
                                   String username,
                                   String nome,
                                   String cognome,
                                   String ruolo,
                                   List<CambioStatoOperatoreResponse> cambiStato) {
        this.idUtente = idUtente;
        this.username = username;
        this.nome = nome;
        this.cognome = cognome;
        this.ruolo = ruolo;
        setCambiStato(cambiStato);
    }

    public long getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(long idUtente) {
        this.idUtente = idUtente;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }

    public List<CambioStatoOperatoreResponse> getCambiStato() {
        return cambiStato;
    }

    public void setCambiStato(List<CambioStatoOperatoreResponse> cambiStato) {
        this.cambiStato = cambiStato != null ? new ArrayList<>(cambiStato) : new ArrayList<>();
    }
}
