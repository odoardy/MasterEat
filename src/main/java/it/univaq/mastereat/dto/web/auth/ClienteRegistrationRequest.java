package it.univaq.mastereat.dto.web.auth;

public class ClienteRegistrationRequest {

    private final String username;
    private final String password;
    private final String nome;
    private final String cognome;
    private final String email;
    private final String telefono;
    private final String indirizzo;
    private final String citta;
    private final String cap;

    public ClienteRegistrationRequest(String username,
                                      String password,
                                      String nome,
                                      String cognome,
                                      String email,
                                      String telefono,
                                      String indirizzo,
                                      String citta,
                                      String cap) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.telefono = telefono;
        this.indirizzo = indirizzo;
        this.citta = citta;
        this.cap = cap;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public String getCitta() {
        return citta;
    }

    public String getCap() {
        return cap;
    }
}
