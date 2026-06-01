package it.univaq.mastereat.dto.api.auth;

public class LoginResponse {

    private String token;
    private long idUtente;
    private String username;
    private String ruolo;

    public LoginResponse() {
    }

    public LoginResponse(String token, long idUtente, String username, String ruolo) {
        this.token = token;
        this.idUtente = idUtente;
        this.username = username;
        this.ruolo = ruolo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }
}
