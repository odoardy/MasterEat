package it.univaq.mastereat.dto.web.customer;

public class ClienteProfileUpdateRequest {

    private String nome;
    private String cognome;
    private String email;
    private String telefono;
    private String indirizzo;
    private String citta;
    private String cap;

    public ClienteProfileUpdateRequest() {
    }

    public ClienteProfileUpdateRequest(String nome,
                                       String cognome,
                                       String email,
                                       String telefono,
                                       String indirizzo,
                                       String citta,
                                       String cap) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.telefono = telefono;
        this.indirizzo = indirizzo;
        this.citta = citta;
        this.cap = cap;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public String getCitta() {
        return citta;
    }

    public void setCitta(String citta) {
        this.citta = citta;
    }

    public String getCap() {
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }
}
